package uz.consortgroup.userservice.service.user;

import uz.consortgroup.core.api.v1.dto.user.request.UserBulkSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.request.UserSearchRequest;
import uz.consortgroup.core.api.v1.dto.user.response.UserBulkSearchResponse;
import uz.consortgroup.core.api.v1.dto.user.response.UserSearchResponse;

public interface UserSearchService {
    UserSearchResponse findUserByEmailOrPinfl(UserSearchRequest dto);
    UserBulkSearchResponse bulkUserSearch(UserBulkSearchRequest dto);
}
