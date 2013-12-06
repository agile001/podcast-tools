package co.eckersley.audio.common;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.context.support.XmlWebApplicationContext;

import com.vaadin.server.VaadinServlet;

public class WebApplicationInitializer implements org.springframework.web.WebApplicationInitializer {
    
    private static final XmlWebApplicationContext appContext = new XmlWebApplicationContext();

    public void onStartup(ServletContext servletContext) throws ServletException {
        
        appContext.setConfigLocation("classpath:/META-INF/spring/applicationContext.xml");

        org.springframework.web.context.ContextLoaderListener contextListener = new org.springframework.web.context.ContextLoaderListener(appContext);
        servletContext.addListener(contextListener);
        
        // this is a JPA LazyInitialisationException/'session no longer active' related filter - to keep EM open for request.
        EnumSet<DispatcherType> openV = EnumSet.of(DispatcherType.REQUEST, DispatcherType.INCLUDE, DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.ERROR);
        FilterRegistration.Dynamic openEmFilter = servletContext.addFilter("oemInView", org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter.class);
        openEmFilter.setInitParameter("entityManagerFactoryBeanName", "entityManagerFactory");
        openEmFilter.addMappingForUrlPatterns(openV, true, "/*");
        openEmFilter.setAsyncSupported(true);

        VaadinServlet vaadinServlet = new VaadinServlet();
        ServletRegistration.Dynamic vaadinServletDispatcher = servletContext.addServlet("vaadinServlet", vaadinServlet);
        vaadinServletDispatcher.setInitParameter("UI", "co.eckersley.audio.podcast.ui.PodcastUI");
        vaadinServletDispatcher.addMapping("/*");
        
        servletContext.setInitParameter("productionMode", "true");
    }
    
    public static XmlWebApplicationContext getApplicationContext() {
        return appContext;
    }
}
