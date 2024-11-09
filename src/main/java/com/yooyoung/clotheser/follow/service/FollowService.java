package com.yooyoung.clotheser.follow.service;

import com.yooyoung.clotheser.follow.domain.Follow;
import com.yooyoung.clotheser.follow.dto.FollowListResponse;
import com.yooyoung.clotheser.follow.repository.FollowRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.notification.domain.NotificationType;
import com.yooyoung.clotheser.notification.dto.NotificationRequest;
import com.yooyoung.clotheser.notification.service.NotificationService;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {
    private final AESUtil aesUtil;
    private final NotificationService notificationService;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    /* 팔로우 생성 */
    public BaseResponseStatus createFollowing(User follower, String userSid) throws BaseException {
        follower.checkIsFirstLogin();
        follower.checkIsSuspended();

        Long userId = aesUtil.decryptUserSid(userSid);
        User followee = userRepository.findByIdAndDeletedAtNull(userId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER, NOT_FOUND));

        boolean isMyself = follower.getId().equals(followee.getId());
        if (isMyself) {
            throw new BaseException(FORBIDDEN_FOLLOW_MYSELF, FORBIDDEN);
        }

        boolean hasFollowed = followRepository.existsByFollowerIdAndFolloweeIdAndDeletedAtNull(
                follower.getId(), followee.getId());
        if (hasFollowed) {
            throw new BaseException(FOLLOW_EXISTS, CONFLICT);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();
        followRepository.save(follow);

        sendFCMNotification(follower, followee);

        return SUCCESS;
    }

    private void sendFCMNotification(User follower, User followee) throws BaseException {
        String message = follower.getNickname() + " 님이 회원 님을 팔로우하였습니다.";
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .user(followee)
                .type(NotificationType.FOLLOW)
                .image(follower.getProfileUrl())
                .sourceId(follower.getId())
                .title("팔로우")
                .content(message)
                .build();
        notificationService.sendNotification(notificationRequest);
    }

    /* 팔로우 삭제 */
    public BaseResponseStatus deleteFollowing(User follower, String userSid) throws BaseException {
        follower.checkIsFirstLogin();
        follower.checkIsSuspended();

        Long followeeId = aesUtil.decryptUserSid(userSid);
        Follow follow = followRepository.findOneByFollowerIdAndFolloweeIdAndDeletedAtNull(follower.getId(), followeeId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_FOLLOW, NOT_FOUND));

        follow.delete();
        followRepository.save(follow);

        return SUCCESS;
    }

    /* 나의 팔로워 목록 조회 */
    public List<FollowListResponse> getMyFollowers(User user) throws BaseException {
        user.checkIsFirstLogin();
        user.checkIsSuspended();

        List<Follow> followerList = followRepository.findAllByFolloweeIdAndDeletedAtNullOrderByCreatedAtDesc(user.getId());
        List<FollowListResponse> responseList = new ArrayList<>();

        for (Follow follow : followerList) {
            User follower = follow.getFollower();
            String userSid = aesUtil.encryptUserId(follower.getId());
            boolean isFollowing = followRepository.existsByFollowerIdAndFolloweeIdAndDeletedAtNull(
                 user.getId(), follower.getId()
            );
            FollowListResponse response = FollowListResponse.builder()
                    .userSid(userSid)
                    .nickname(follower.getNickname())
                    .profileUrl(follower.getProfileUrl())
                    .isFollowing(isFollowing)
                    .build();
            responseList.add(response);
        }

        return responseList;
    }

    /* 나의 팔로잉 목록 조회 */
    public List<FollowListResponse> getMyFollowings(User user) throws BaseException {
        user.checkIsFirstLogin();
        user.checkIsSuspended();

        List<Follow> followerList = followRepository.findAllByFollowerIdAndDeletedAtNullOrderByCreatedAtDesc(user.getId());
        List<FollowListResponse> responseList = new ArrayList<>();

        for (Follow follow : followerList) {
            User followee = follow.getFollowee();
            String userSid = aesUtil.encryptUserId(followee.getId());
            FollowListResponse response = FollowListResponse.builder()
                    .userSid(userSid)
                    .nickname(followee.getNickname())
                    .profileUrl(followee.getProfileUrl())
                    .isFollowing(true)
                    .build();
            responseList.add(response);
        }

        return responseList;
    }
}
