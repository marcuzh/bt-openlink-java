package com.bt.openlink;

import com.bt.openlink.type.CallId;
import com.bt.openlink.type.InterestId;
import com.bt.openlink.type.ProfileId;

@SuppressWarnings("ConstantConditions")
public class Fixtures {
    public static final CallId CALL_ID = CallId.from("test-call-id").get();
    public static ProfileId PROFILE_ID = ProfileId.from("test-profile-id").get();
    public static InterestId INTEREST_ID = InterestId.from("test-interest-id").get();
}