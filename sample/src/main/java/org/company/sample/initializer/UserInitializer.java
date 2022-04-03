package org.company.sample.initializer;

import io.rocketbase.commons.dto.appuser.AppUserCreate;
import io.rocketbase.commons.model.AppUserEntity;
import io.rocketbase.commons.service.user.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private AppUserService appUserService;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        AppUserEntity adminUser = appUserService.getByUsername("admin");
        if (adminUser == null) {
            appUserService.initializeUser(AppUserCreate.builder()
                    .username("admin")
                    .password("admin")
                    .email("info@rocketbase.io")
                    .admin(true)
                    .enabled(true)
                    .build());
        }


        AppUserEntity userUser = appUserService.getByUsername("admin");
        if (userUser == null) {
            appUserService.initializeUser(AppUserCreate.builder()
                    .username("user")
                    .password("user")
                    .email("team@rocketbase.io")
                    .admin(false)
                    .enabled(true)
                    .build());
        }

    }
}
