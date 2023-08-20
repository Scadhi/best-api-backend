package cn.ichensw.bestclientsdk;

import cn.ichensw.bestclientsdk.client.BestApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Best API 客户端配置类
 * @author nero
 */
@Data
@Configuration
@ConfigurationProperties("nero.client")
@ComponentScan
public class BestApiClientConfig {

    private String accessKey;

    private String secretKey;

    /**
     * 此处方法取名无所谓的，不影响任何地方
     *
     * @return
     */
    @Bean
    public BestApiClient getApiClient() {
        return new BestApiClient(accessKey, secretKey);
    }
}
