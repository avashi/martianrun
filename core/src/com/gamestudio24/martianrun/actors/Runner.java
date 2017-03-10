/*
 * Copyright (c) 2014. William Mora
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gamestudio24.martianrun.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.gamestudio24.martianrun.box2d.RunnerUserData;
import com.gamestudio24.martianrun.enums.Difficulty;
import com.gamestudio24.martianrun.enums.GameState;
import com.gamestudio24.martianrun.utils.AssetsManager;
import com.gamestudio24.martianrun.utils.AudioUtils;
import com.gamestudio24.martianrun.utils.Constants;
import com.gamestudio24.martianrun.utils.GameManager;

import static com.gamestudio24.martianrun.utils.Constants.GROUND_HEIGHT;
import static com.gamestudio24.martianrun.utils.Constants.RUNNER_Y;

public class Runner extends GameActor implements bluetoothConnector{

    private boolean dodging;
    private boolean jumping;
    private boolean hit;
    private Animation runningAnimation;
    private TextureRegion jumpingTexture;
    private TextureRegion dodgingTexture;
    private TextureRegion hitTexture;
    private float stateTime;

    private Sound jumpSound;
    private Sound hitSound;

    private int jumpCount;

    public Runner(Body body) {
        super(body);
        jumpCount = 0;
        runningAnimation = AssetsManager.getAnimation(Constants.RUNNER_RUNNING_ASSETS_ID);
        stateTime = 0f;
        jumpingTexture = AssetsManager.getTextureRegion(Constants.RUNNER_JUMPING_ASSETS_ID);
        dodgingTexture = AssetsManager.getTextureRegion(Constants.RUNNER_DODGING_ASSETS_ID);
        hitTexture = AssetsManager.getTextureRegion(Constants.RUNNER_HIT_ASSETS_ID);
        jumpSound = AudioUtils.getInstance().getJumpSound();
        hitSound = AudioUtils.getInstance().getHitSound();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        float x = screenRectangle.x - (screenRectangle.width * 0.1f);
        float y = screenRectangle.y;
        float width = screenRectangle.width * 1.2f;

        if (dodging) {
            batch.draw(dodgingTexture, x, y + screenRectangle.height / 4, width, screenRectangle.height * 3 / 4);
        } else if (hit) {
            // When he's hit we also want to apply rotation if the body has been rotated
            batch.draw(hitTexture, x, y, width * 0.5f, screenRectangle.height * 0.5f, width, screenRectangle.height, 1f,
                    1f, (float) Math.toDegrees(body.getAngle()));
        } else if (jumping) {
            batch.draw(jumpingTexture, x, y, width, screenRectangle.height);
        } else {
            // Running
            if (GameManager.getInstance().getGameState() == GameState.RUNNING) {
                stateTime += Gdx.graphics.getDeltaTime();
            }
            batch.draw(runningAnimation.getKeyFrame(stateTime, true), x, y, width, screenRectangle.height);
        }
    }

    @Override
    public RunnerUserData getUserData() {
        return (RunnerUserData) userData;
    }
//*****************************************************************************************************************************************
//*****************************************************************************************************************************************
    //*****************************************************************************************************************************************
    //*****************************************************************************************************************************************
    //Once bluetooth works: will change Jump to essentially the same thing as dodge but in opposite direction.. somehow disable gravity? stuck here
    //nevermind.. found a fix: only can move down and then back to normal... so just get rid of jump feature? <<probably

    public void jump(double dataIn2) {

//        //changed up what jump method actually does
//        if (!(jumping || hit) || dataIn2<0) {
////            body.setTransform(getUserData().getDodgePosition(), getUserData().getDodgeAngle());
////            dodging = true;
//            runner.stopDodge();
//        }

        if (!(jumping || dodging || hit)) {
            body.applyLinearImpulse(getUserData().getJumpingLinearImpulse(), body.getWorldCenter(), true);
            jumping = true;
//            adding to this to levetate?
//            body.setTransform(2,4,0);
            AudioUtils.getInstance().playSound(jumpSound);
            jumpCount++;
            // ********************************************************************************************************************************8
//            body.setTransform(getUserData().getJumpingLinearImpulse(), getUserData().getDodgeAngle()*-1);
//            jumping = true;
//            AudioUtils.getInstance().playSound(jumpSound);
//            jumpCount++;
            //**********************************************************************************************************************************

        }

    }

    //messed around with this... can we get the character to stay levetated?.. changed false to true.. bad idea.. just made the dood not even move..
    //none of the controls worked
    public void landed() {
        jumping = false;
    }
/*


*
* *
* *
* *
* *
* **
* *
 */
    @Override
    public void dodge(double dataIn) {
        if (!(jumping || hit) || dataIn<0) {
            body.setTransform(getUserData().getDodgePosition(), getUserData().getDodgeAngle());
            dodging = true;
        }
    }

    @Override

    public void stopDodge() {
        dodging = false;
        // If the runner is hit don't force him back to the running position
        if (!hit) {
            body.setTransform(getUserData().getRunningPosition(), 0f);
        }
    }

    public boolean isDodging() {
        return dodging;
    }

    public void hit() {
        body.applyAngularImpulse(getUserData().getHitAngularImpulse(), true);
        hit = true;
        AudioUtils.getInstance().playSound(hitSound);
    }

    public boolean isHit() {
        return hit;
    }

    public void onDifficultyChange(Difficulty newDifficulty) {
        setGravityScale(newDifficulty.getRunnerGravityScale());
        getUserData().setJumpingLinearImpulse(newDifficulty.getRunnerJumpingLinearImpulse());
    }

    public void setGravityScale(float gravityScale) {
        body.setGravityScale(gravityScale);
        body.resetMassData();
    }

    public int getJumpCount() {
        return jumpCount;
    }
}
