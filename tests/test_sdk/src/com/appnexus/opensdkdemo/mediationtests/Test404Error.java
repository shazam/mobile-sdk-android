/*
 *    Copyright 2013 APPNEXUS INC
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.appnexus.opensdkdemo.mediationtests;

import android.test.AndroidTestCase;
import com.appnexus.opensdk.*;
import com.appnexus.opensdk.utils.Clog;
import com.appnexus.opensdk.utils.Settings;
import com.appnexus.opensdkdemo.testviews.DummyView;
import com.appnexus.opensdkdemo.testviews.SuccessfulMediationView;
import com.appnexus.opensdkdemo.util.InstanceLock;
import com.appnexus.opensdkdemo.util.TestUtil;

public class Test404Error extends AndroidTestCase implements AdRequester {
    /**
     * NOTE: requires commenting out return code in MAVC's resultCB handler
     * to allow for multiple successes.
     */
    String old_base_url;
    AdRequest shouldWork;
    String shouldWorkPlacement = "9a";
    InstanceLock lock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        old_base_url = Settings.getSettings().BASE_URL;
        Settings.getSettings().BASE_URL = TestUtil.MEDIATION_TEST_URL;
        Clog.d(TestUtil.testLogTag, "BASE_URL set to " + Settings.getSettings().BASE_URL);
        shouldWork = new AdRequest(this, null, null, null, shouldWorkPlacement, null, null, 320, 50, -1, -1, null, null, null, true, null, false, false);
        SuccessfulMediationView.didPass = false;
        DummyView.createView(getContext());
        lock = new InstanceLock();
    }

    @Override
    protected void tearDown() throws Exception {
        Clog.d(TestUtil.testLogTag, "tear down");
        Settings.getSettings().BASE_URL = old_base_url;
        super.tearDown();
    }

    public void testMediationThen404() {
        // Create a AdRequest which will request a mediated response to
        // instantiate the SuccessfulMediationView. The (success) result_cb
        // should return a 404 error, which should fail instantiation

        shouldWork.execute();
        lock.pause(10000);
        shouldWork.cancel(true);

        assertEquals(true, SuccessfulMediationView.didPass);
    }

    @Override
    public void failed(AdRequest request) {
        Clog.d(TestUtil.testLogTag, "request failed: " + request);
        SuccessfulMediationView.didPass = false;
        lock.unpause();
    }

    @Override
    public void onReceiveResponse(AdResponse response) {
        // response should be a regular valid mediatied ad
        Clog.d(TestUtil.testLogTag, "received response: " + response);
        MediatedBannerAdViewController output = MediatedBannerAdViewController.create(
                null, this, response.getMediatedAds().pop(), null);
    }

    @Override
    public AdView getOwner() {
        return null;
    }

    @Override
    public void dispatchResponse(AdResponse response) {
        // response should be a 404
        Clog.d(TestUtil.testLogTag, "dispatch " + response.toString());
        MediatedBannerAdViewController output = MediatedBannerAdViewController.create(
                null, this, response.getMediatedAds().pop(), null);

        // instantiation should fail, so this should be null
        assertNull(output);
        lock.unpause();
    }

}
