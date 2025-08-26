
package ra.edu.service.imp;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        File temp = File.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(temp);
        try {
            Map<String, Object> result = cloudinary.uploader().upload(temp, ObjectUtils.emptyMap());
            return result.get("secure_url").toString();
        } finally {
            temp.deleteOnExit();
        }
    }
}
