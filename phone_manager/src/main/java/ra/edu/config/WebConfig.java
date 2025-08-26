package ra.edu.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dqqwz9yjr",
                "api_key", "443134321763158",
                "api_secret", "J1M5shGIwgobLnjE5UqnZ_kKETc",
                "secure", true
        ));
    }
}
