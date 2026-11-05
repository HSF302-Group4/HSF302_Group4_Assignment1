package com.fu.cinema.desktop;

import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.View;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final SceneManager sceneManager;                 // TODO 34

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        sceneManager.init(event.getStage());
        sceneManager.switchTo(View.LOGIN);
    }
}
