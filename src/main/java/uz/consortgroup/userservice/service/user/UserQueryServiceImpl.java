package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uz.consortgroup.core.api.v1.dto.user.response.UserFullInfoResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.exception.InvalidTokenException;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.repository.UserRepository;
import uz.consortgroup.userservice.service.operation.UserOperationsService;
import uz.consortgroup.userservice.util.JwtUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserOperationsService userOperationsService;
    private final JwtUtils jwtUtils;

    @Override
    public Page<UserFullInfoResponseDto> getAllUsersFullInfo(Pageable pageable) {
        log.debug("Fetching all users with pageable: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<User> page = userRepository.findAll(pageable);
        log.info("Fetched {} users (total elements: {})", page.getNumberOfElements(), page.getTotalElements());
        return page.map(userMapper::toDto);
    }

    @Override
    public UserFullInfoResponseDto getUserFullInfoById(UUID userId) {
        log.debug("Fetching user full info by ID: {}", userId);
        User user = userOperationsService.getUserFromDbAndCacheById(userId);
        log.info("Fetched user with ID: {}, email: {}", user.getId(), user.getEmail());
        return userMapper.toDto(user);
    }

    @Override
    public UserFullInfoResponseDto getUserFullInfoByToken(String rawToken) {
        String token = stripBearer(rawToken);

        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Header 'token' is required");
        }

        if (!jwtUtils.validateJwtToken(token)) {
            log.warn("Invalid JWT provided");
            throw new InvalidTokenException("Invalid JWT token");
        }

        String userIdStr = jwtUtils.getClaimFromJwtToken(token, "userId", String.class);
        UUID userId = tryParseUuid(userIdStr);
        if (userId != null) {
            return getUserFullInfoById(userId);
        }

        String email = jwtUtils.getClaimFromJwtToken(token, "email", String.class);
        if (email == null || email.isBlank()) {
            email = jwtUtils.getUserNameFromJwtToken(token);
        }
        if (email != null && !email.isBlank()) {
            User user = userOperationsService.findUserByEmail(email.trim().toLowerCase());
            return userMapper.toDto(user);
        }

        log.warn("JWT doesn't contain recognizable user identifier (userId/email/subject)");
        throw new IllegalArgumentException("Token doesn't contain user identifier");
    }

    private static String stripBearer(String token) {
        if (token == null) return "";
        String t = token.trim();
        return (t.regionMatches(true, 0, "Bearer ", 0, 7)) ? t.substring(7) : t;
    }

    private static UUID tryParseUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
