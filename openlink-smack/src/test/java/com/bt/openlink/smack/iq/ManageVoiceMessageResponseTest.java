package com.bt.openlink.smack.iq;

import com.bt.openlink.CoreFixtures;
import com.bt.openlink.ManageVoiceMessageFixtures;
import com.bt.openlink.OpenlinkXmppNamespace;
import com.bt.openlink.smack.Fixtures;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

public class ManageVoiceMessageResponseTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() throws Exception {
        ProviderManager.addIQProvider("command", OpenlinkXmppNamespace.XMPP_COMMANDS.uri(), new OpenlinkIQProvider());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ProviderManager.removeIQProvider("command", OpenlinkXmppNamespace.XMPP_COMMANDS.uri());
    }

    @Test
    public void canBuildAManageVoiceMessageQueryStanza() throws Exception {

        final ManageVoiceMessageResponse result = ManageVoiceMessageResponse.Builder.start()
                .setTo(Fixtures.TO_JID)
                .setFrom(Fixtures.FROM_JID)
                .setDeviceStatus(ManageVoiceMessageFixtures.DEVICE_STATUS_QUERY)
                .build();

        assertThat(result.getDeviceStatus().get(), is(ManageVoiceMessageFixtures.DEVICE_STATUS_QUERY));
    }

    @Test
    public void willGenerateAnXmppManageVoiceMessageQueryStanza() throws Exception {

        final ManageVoiceMessageResponse result = ManageVoiceMessageResponse.Builder.start()
                .setId(CoreFixtures.STANZA_ID)
                .setTo(Fixtures.TO_JID)
                .setFrom(Fixtures.FROM_JID)
                .setDeviceStatus(ManageVoiceMessageFixtures.DEVICE_STATUS_QUERY)
                .build();

        assertThat(result.toXML().toString(), isIdenticalTo(ManageVoiceMessageFixtures.MANAGE_VOICE_MESSAGE_QUERY_RESULT).ignoreWhitespace());
    }

    @Test
    public void willGenerateAnXmppManageVoiceMessagePlaybackStanza() throws Exception {

        final ManageVoiceMessageResponse result = ManageVoiceMessageResponse.Builder.start()
                .setId(CoreFixtures.STANZA_ID)
                .setTo(Fixtures.TO_JID)
                .setFrom(Fixtures.FROM_JID)
                .setDeviceStatus(ManageVoiceMessageFixtures.DEVICE_STATUS_PLAYBACK)
                .build();

        assertThat(result.toXML().toString(), isIdenticalTo(ManageVoiceMessageFixtures.MANAGE_VOICE_MESSAGE_PLAYBACK_RESULT).ignoreWhitespace());
    }

    @Test
    public void willGenerateAnXmppManageVoiceMessageEditStanza() throws Exception {

        final ManageVoiceMessageResponse result = ManageVoiceMessageResponse.Builder.start()
                .setId(CoreFixtures.STANZA_ID)
                .setTo(Fixtures.TO_JID)
                .setFrom(Fixtures.FROM_JID)
                .setDeviceStatus(ManageVoiceMessageFixtures.DEVICE_STATUS_EDIT)
                .build();

        assertThat(result.toXML().toString(), isIdenticalTo(ManageVoiceMessageFixtures.MANAGE_VOICE_MESSAGE_EDIT_RESULT).ignoreWhitespace());
    }

    @Test
    public void willGenerateAnXmppManageVoiceMessageRecordStanza() throws Exception {

        final ManageVoiceMessageResponse result = ManageVoiceMessageResponse.Builder.start()
                .setId(CoreFixtures.STANZA_ID)
                .setTo(Fixtures.TO_JID)
                .setFrom(Fixtures.FROM_JID)
                .setDeviceStatus(ManageVoiceMessageFixtures.DEVICE_STATUS_RECORD)
                .build();

        assertThat(result.toXML().toString(), isIdenticalTo(ManageVoiceMessageFixtures.MANAGE_VOICE_MESSAGE_RECORD_RESULT).ignoreWhitespace());
    }

    @Test
    public void willEnsureTheStanzaHasADeviceStatus() throws Exception {

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("The stanza 'deviceStatus' has not been set");
        ManageVoiceMessageResponse.Builder.start()
                .setTo(Fixtures.TO_JID)
                .setFrom(Fixtures.FROM_JID)
                .build();
    }

    @Test
    public void willParseAManageVoiceMessageQueryStanza() throws Exception {

        final ManageVoiceMessageResponse result = PacketParserUtils.parseStanza(ManageVoiceMessageFixtures.MANAGE_VOICE_MESSAGE_QUERY_RESULT);

        assertThat(result.getStanzaId(), is(CoreFixtures.STANZA_ID));
        assertThat(result.getTo(), is(Fixtures.TO_JID));
        assertThat(result.getFrom(), is(Fixtures.FROM_JID));
        assertReflectionEquals(ManageVoiceMessageFixtures.DEVICE_STATUS_QUERY, result.getDeviceStatus().get());
        assertThat(result.getParseErrors().size(), is(0));
    }

    @Test
    public void willParseAManageVoiceMessagePlaybackStanza() throws Exception {

        final ManageVoiceMessageResponse result = PacketParserUtils.parseStanza(ManageVoiceMessageFixtures.MANAGE_VOICE_MESSAGE_PLAYBACK_RESULT);

        assertThat(result.getStanzaId(), is(CoreFixtures.STANZA_ID));
        assertThat(result.getTo(), is(Fixtures.TO_JID));
        assertThat(result.getFrom(), is(Fixtures.FROM_JID));
        assertReflectionEquals(ManageVoiceMessageFixtures.DEVICE_STATUS_PLAYBACK, result.getDeviceStatus().get());
        assertThat(result.getParseErrors().size(), is(0));
    }

    @Test
    public void willParseAManageVoiceMessageEditStanza() throws Exception {

        final ManageVoiceMessageResponse result = PacketParserUtils.parseStanza(ManageVoiceMessageFixtures.MANAGE_VOICE_MESSAGE_EDIT_RESULT);

        assertThat(result.getStanzaId(), is(CoreFixtures.STANZA_ID));
        assertThat(result.getTo(), is(Fixtures.TO_JID));
        assertThat(result.getFrom(), is(Fixtures.FROM_JID));
        assertReflectionEquals(ManageVoiceMessageFixtures.DEVICE_STATUS_EDIT, result.getDeviceStatus().get());
        assertThat(result.getParseErrors().size(), is(0));
    }
}