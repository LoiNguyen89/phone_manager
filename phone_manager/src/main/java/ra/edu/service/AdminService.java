package ra.edu.service;

import ra.edu.model.entity.Admin;

public interface AdminService {
    Admin register(String username, String rawPassword, String confirmPassword);
    Admin login(String username, String rawPassword);
}
