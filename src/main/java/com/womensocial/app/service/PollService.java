package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.model.dto.request.CreatePollRequest;
import com.womensocial.app.model.dto.request.VoteRequest;
import com.womensocial.app.model.dto.response.PollResponse;
import com.womensocial.app.model.entity.*;
import com.womensocial.app.repository.PollRepository;
import com.womensocial.app.repository.PollVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserService userService;

    @Transactional
    public Poll createPollForPost(Post post, CreatePollRequest request) {
        return savePoll(request, post, null);
    }

    @Transactional
    public Poll createPollForAnonymousPost(AnonymousPost post, CreatePollRequest request) {
        return savePoll(request, null, post);
    }

    @Transactional(readOnly = true)
    public PollResponse getPoll(Long pollId, Long userId) {
        Poll poll = findById(pollId);
        return enrichWithVoteStatus(PollResponse.from(poll), userId, poll);
    }

    @Transactional
    public PollResponse vote(Long pollId, Long userId, VoteRequest request) {
        if (pollVoteRepository.existsByPollIdAndUserId(pollId, userId)) {
            throw new BadRequestException("You have already voted on this poll");
        }

        Poll poll = findById(pollId);
        PollOption option = poll.getOptions().stream()
                .filter(o -> o.getId().equals(request.getOptionId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Poll option not found"));

        User user = userService.findUserById(userId);
        pollVoteRepository.save(PollVote.builder()
                .poll(poll)
                .option(option)
                .user(user)
                .build());

        option.setVoteCount(option.getVoteCount() + 1);
        poll.setTotalVotes(poll.getTotalVotes() + 1);
        Poll saved = pollRepository.save(poll);

        PollResponse response = PollResponse.from(saved);
        response.setVotedByMe(true);
        response.setMyVotedOptionId(option.getId());
        return response;
    }

    private Poll savePoll(CreatePollRequest request, Post post, AnonymousPost anonymousPost) {
        Poll poll = Poll.builder()
                .post(post)
                .anonymousPost(anonymousPost)
                .question(request.getQuestion())
                .build();

        List<PollOption> options = request.getOptions().stream()
                .map(text -> PollOption.builder()
                        .poll(poll)
                        .optionText(text)
                        .build())
                .toList();

        poll.setOptions(options);
        return pollRepository.save(poll);
    }

    private PollResponse enrichWithVoteStatus(PollResponse response, Long userId, Poll poll) {
        if (userId != null && pollVoteRepository.existsByPollIdAndUserId(poll.getId(), userId)) {
            response.setVotedByMe(true);
        }
        return response;
    }

    public Poll findById(Long pollId) {
        return pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found with id: " + pollId));
    }
}
