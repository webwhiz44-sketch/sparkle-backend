package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.Poll;
import com.womensocial.app.model.entity.PollOption;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PollResponse {

    private Long id;
    private String question;
    private int totalVotes;
    private boolean votedByMe;
    private Long myVotedOptionId;
    private List<PollOptionResponse> options;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class PollOptionResponse {
        private Long id;
        private String optionText;
        private int voteCount;
        private double percentage;
    }

    public static PollResponse from(Poll poll) {
        List<PollOptionResponse> options = poll.getOptions() == null ? List.of() :
                poll.getOptions().stream()
                        .map(opt -> PollOptionResponse.builder()
                                .id(opt.getId())
                                .optionText(opt.getOptionText())
                                .voteCount(opt.getVoteCount())
                                .percentage(poll.getTotalVotes() > 0
                                        ? (double) opt.getVoteCount() / poll.getTotalVotes() * 100
                                        : 0.0)
                                .build())
                        .collect(Collectors.toList());

        return PollResponse.builder()
                .id(poll.getId())
                .question(poll.getQuestion())
                .totalVotes(poll.getTotalVotes())
                .options(options)
                .createdAt(poll.getCreatedAt())
                .build();
    }
}
