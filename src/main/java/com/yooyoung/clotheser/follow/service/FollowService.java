package com.yooyoung.clotheser.follow.service;

import com.yooyoung.clotheser.follow.domain.Follow;
import com.yooyoung.clotheser.follow.repository.FollowRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {
    private final AESUtil aesUtil;
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

        return SUCCESS;
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
}
