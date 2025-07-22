package org.example.shoppingweb.security;

import org.apache.catalina.Context;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

public class config {
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addContextCustomizers((Context context) -> {
            context.setAllowCasualMultipartParsing(true);
            context.getServletContext().setInitParameter(
                    "org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax",
                    "10000" //
            );
        });
        return factory;
    }
}
