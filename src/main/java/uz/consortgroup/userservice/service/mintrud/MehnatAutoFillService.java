package uz.consortgroup.userservice.service.mintrud;

import uz.consortgroup.userservice.entity.User;

public interface MehnatAutoFillService {
    void tryFetchDataFromMehnat(User user);
}
