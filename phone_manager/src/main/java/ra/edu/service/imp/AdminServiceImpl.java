package ra.edu.service.imp;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import ra.edu.model.entity.Admin;
import ra.edu.repo.AdminRepository;
import ra.edu.service.AdminService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;

    @Override
    public Admin register(String username, String rawPassword, String confirmPassword) {
        // Kiểm tra username đã tồn tại
        if (adminRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        // Kiểm tra password và confirmPassword
        if (!rawPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match!");
        }

        // Validate password format
        if (!rawPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{6,}$")) {
            throw new RuntimeException(
                    "Password must contain at least 6 characters, uppercase, lowercase, number, special char"
            );
        }

        // Mã hóa mật khẩu với BCrypt
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        Admin admin = Admin.builder()
                .username(username)
                .password(hashed)
                .build();
        return adminRepository.save(admin);
    }

    @Override
    public Admin login(String username, String rawPassword) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (BCrypt.checkpw(rawPassword, admin.getPassword())) {
                return admin;
            }
        }
        throw new RuntimeException("Invalid username or password!");
    }





}


