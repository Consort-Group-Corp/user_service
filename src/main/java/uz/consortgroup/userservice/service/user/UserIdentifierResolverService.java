package uz.consortgroup.userservice.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.repository.UserRepository;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserIdentifierResolverService {

    private static final Pattern PINFL_14 = Pattern.compile("^\\d{14}$");

    private final UserRepository userRepository;

    public UserBulkSearchResponse resolveBulk(UserBulkSearchRequest request) {
        List<String> raw = request == null || request.getQueries() == null
                ? List.of()
                : request.getQueries().stream()
                    .map(UserSearchRequest::getQuery)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .toList();

        if (raw.isEmpty()) {
            log.info("bulkSearch: empty queries");
            return UserBulkSearchResponse.builder().users(List.of()).build();
        }

        Set<String> emails = raw.stream()
                .filter(v -> v.contains("@"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> pinfls = raw.stream()
                .filter(v -> PINFL_14.matcher(v).matches())
                .collect(Collectors.toSet());

        List<User> byEmails = emails.isEmpty()
                ? List.of()
                : userRepository.findByEmailInIgnoreCase(emails);

        List<User> byPinfls = pinfls.isEmpty()
                ? List.of()
                : userRepository.findByPinflIn(pinfls);


        Map<UUID, User> unique = Stream.concat(byEmails.stream(), byPinfls.stream())
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<UserSearchResponse> out = unique.values().stream()
                .map(u -> UserSearchResponse.builder()
                        .userId(u.getId())
                        .email(u.getEmail())
                        .pinfl(u.getPinfl())
                        .role(u.getRole())
                        .build())
                .toList();

        log.info("bulkSearch: in={}, found={}", raw.size(), out.size());
        return UserBulkSearchResponse.builder().users(out).build();
    }
}
