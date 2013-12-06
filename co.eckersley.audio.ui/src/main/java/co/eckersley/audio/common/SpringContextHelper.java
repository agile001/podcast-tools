package co.eckersley.audio.common;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringContextHelper {
    
    private ApplicationContext context;

    public SpringContextHelper(ServletContext servletContext) {
        /*
         * ServletContext servletContext = ((WebApplicationContext)
         * application.getContext()) .getHttpSession().getServletContext();
         */
        context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final String beanRef) {
        return (T) context.getBean(beanRef);
    }

    public <T> T getBean(final Class<T> requiredType) {
        return context.getBean(requiredType);
    }
}
