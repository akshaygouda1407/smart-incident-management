package com.smartims.service;

import com.smartims.dto.IssueCommentResponse;

import java.util.List;

public interface IssueCommentService {

    IssueCommentResponse addComment(Long issueId, String comment);

    List<IssueCommentResponse> getComments(Long issueId);
}
