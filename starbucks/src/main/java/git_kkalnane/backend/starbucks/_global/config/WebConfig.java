package git_kkalnane.backend.starbucks._global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.max-age}")
    private int maxAge;

    /**
     * AuthInterceptor를 적용 또는 제외할 URI를 설정하는 메서드
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")   // Swagger 관련 URI 제외
                .excludePathPatterns("/auth/login", "/members/signup");      // 회원가입, 로그인 엔드포인트 제외
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // CORS 적용 범위 지정
                .allowedOrigins(allowedOrigins)                   // 허용할 Origin(출처, 도메인)을 지정하는 메서드 (와일드카드 사용 불가능)
                .allowedOriginPatterns(allowedOrigins)          // 허용할 Origin(출처, 도메인) 패턴을 지정하는 메서드
                .allowedMethods(allowedMethods.split(","))  // 클라이언트가 서버로 요청을 보낼 시 허용할 HTTP 메서드를 지정하는 메서드
                .allowedHeaders(allowedHeaders)                   // 클라이언트가 서버로 요청을 보낼 시 허용할 헤더를 지정하는 메서드
                .exposedHeaders("Authorization", "Set-Cookie")    // 서버가 프론트엔드로 응답을 보낼 시 노출할 헤더를 지정하는 메서드
                .allowCredentials(true)                           // 클라이언트가 서버로 요청을 보낼 시 허용할 자격 증명 허용 여부를 지정하는 메서드
                .maxAge(maxAge);                                  // preflight 요청의 캐시 시간을 지정하는 메서드
    }
}
