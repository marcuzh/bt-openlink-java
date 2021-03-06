package com.bt.openlink.smack.internal;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jivesoftware.smack.packet.IQ.IQChildElementXmlStringBuilder;
import org.jivesoftware.smack.util.ParserUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.bt.openlink.OpenlinkXmppNamespace;
import com.bt.openlink.type.Call;
import com.bt.openlink.type.CallDirection;
import com.bt.openlink.type.CallFeature;
import com.bt.openlink.type.CallFeatureBoolean;
import com.bt.openlink.type.CallFeatureDeviceKey;
import com.bt.openlink.type.CallFeatureSpeakerChannel;
import com.bt.openlink.type.CallFeatureTextValue;
import com.bt.openlink.type.CallFeatureVoiceRecorder;
import com.bt.openlink.type.CallId;
import com.bt.openlink.type.CallState;
import com.bt.openlink.type.CallStatus;
import com.bt.openlink.type.Changed;
import com.bt.openlink.type.ConferenceId;
import com.bt.openlink.type.DeviceId;
import com.bt.openlink.type.DeviceKey;
import com.bt.openlink.type.DeviceStatus;
import com.bt.openlink.type.FeatureId;
import com.bt.openlink.type.FeatureType;
import com.bt.openlink.type.InterestId;
import com.bt.openlink.type.ManageVoiceMessageAction;
import com.bt.openlink.type.OriginatorReference;
import com.bt.openlink.type.Participant;
import com.bt.openlink.type.ParticipantCategory;
import com.bt.openlink.type.ParticipantType;
import com.bt.openlink.type.PhoneNumber;
import com.bt.openlink.type.ProfileId;
import com.bt.openlink.type.RecorderChannel;
import com.bt.openlink.type.RecorderNumber;
import com.bt.openlink.type.RecorderPort;
import com.bt.openlink.type.RecorderType;
import com.bt.openlink.type.RequestAction;
import com.bt.openlink.type.Site;
import com.bt.openlink.type.TelephonyCallId;
import com.bt.openlink.type.UserId;
import com.bt.openlink.type.VoiceMessage;
import com.bt.openlink.type.VoiceMessageFeature;
import com.bt.openlink.type.VoiceMessageStatus;
import com.bt.openlink.type.VoiceRecorderInfo;

public final class SmackPacketUtil {

    private static final DateTimeFormatter JAVA_UTIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
    private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final String ATTRIBUTE_NUMBER = "number";
    private static final String ATTRIBUTE_DESTINATION = "destination";
    private static final String ATTRIBUTE_DIRECTION = "direction";
    private static final String ATTRIBUTE_START_TIME = "start";
    private static final String ATTRIBUTE_TIMESTAMP = "timestamp";
    private static final String ATTRIBUTE_DURATION = "duration";
    private static final String ATTRIBUTE_LABEL = "label";
    private static final String ATTRIBUTE_XMLNS = "xmlns";
    private static final String ELEMENT_DEVICEKEYS = "devicekeys";
    private static final String ELEMENT_NUMBER = "number";
    private static final String ELEMENT_CALLER = "caller";
    private static final String ELEMENT_CALLED = "called";
    private static final String ELEMENT_PROFILE = "profile";
    private static final String ELEMENT_ORIGINATOR_REF = "originator-ref";
    private static final String ELEMENT_PROPERTY = "property";
    private static final String ELEMENT_ACTIONS = "actions";
    private static final String ELEMENT_PARTICIPANTS = "participants";
    private static final String ELEMENT_PARTICIPANT = "participant";
    private static final String ELEMENT_FEATURES = "features";
    private static final String ELEMENT_FEATURE = "feature";
    private static final String ELEMENT_SPEAKERCHANNEL = "speakerchannel";
    private static final String ELEMENT_VOICERECORDER = "voicerecorder";
    private static final String ELEMENT_CHANNEL = "channel";
    private static final String ELEMENT_MICROPHONE = "microphone";
    private static final String ELEMENT_MUTE = "mute";
    private static final String ELEMENT_NAME = "name";
    private static final String ELEMENT_CALLSTATUS = "callstatus";
    private static final String ELEMENT_DEVICESTATUS = "devicestatus";
    private static final String ELEMENT_VOICE_MESSAGE = "voicemessage";
    private static final String ELEMENT_STATUS = "status";
    private static final String ELEMENT_ACTION = "action";
    private static final String ELEMENT_MESSAGE_LENGTH = "msglen";
    private static final String ELEMENT_CREATION_DATE = "creationdate";
    private static final String ATTRIBUTE_ONLINE = "online";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ELEMENT_EXTENSION = "exten";
    private static final String ELEMENT_RECORDER_NUMBER = "recnumber";
    private static final String ELEMENT_RECORDER_PORT = "recport";
    private static final String ELEMENT_RECORDER_CHANNEL = "recchan";
    private static final String ELEMENT_RECORDER_TYPE = "rectype";
    private static final String ATTRIBUTE_DEVICENUM = "devicenum";

    private SmackPacketUtil() {
    }

    @Nonnull
    public static Optional<Jid> getSmackJid(@Nullable String jidString) {
        try {
            return jidString == null || jidString.isEmpty() ? Optional.empty() : Optional.of(JidCreate.from(jidString));
        } catch (final XmppStringprepException ignored) {
            return Optional.empty();
        }
    }

    @Nonnull
    public static Optional<Boolean> getBooleanAttribute(
            @Nonnull final XmlPullParser parser,
            @Nonnull final String attributeName,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors) {
        final String attributeValue = parser.getAttributeValue("", attributeName);
        if (attributeValue == null) {
            return Optional.empty();
        } else {
            return getBoolean(attributeValue, attributeName, description, parseErrors);
        }
    }

    @Nonnull
    public static Optional<String> getStringAttribute(@Nonnull final XmlPullParser parser, @Nonnull final String attributeName) {
        return Optional.ofNullable(parser.getAttributeValue("", attributeName));
    }

    @Nonnull
    private static Optional<Long> getLongAttribute(@Nonnull final XmlPullParser parser, @Nonnull final String attributeName) {
        final String attributeValue = parser.getAttributeValue("", attributeName);
        if (attributeValue == null || attributeValue.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(attributeValue));
        } catch (final NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    @Nonnull
    public static Optional<Integer> getIntegerAttribute(@Nonnull final XmlPullParser parser, @Nonnull final String attributeName) {
        final String attributeValue = parser.getAttributeValue("", attributeName);
        if (attributeValue == null || attributeValue.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.valueOf(attributeValue));
        } catch (final NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Site> getSite(final XmlPullParser parser, final List<String> errors, final String description) throws IOException, XmlPullParserException {
        if (parser.getName().equals("site")) {
            final Site.Builder siteBuilder = Site.Builder.start();
            final Optional<Long> siteId = SmackPacketUtil.getLongAttribute(parser, ATTRIBUTE_ID);
            siteId.ifPresent(siteBuilder::setId);
            final Optional<Boolean> isDefaultSite = SmackPacketUtil.getBooleanAttribute(parser, OpenlinkXmppNamespace.TAG_DEFAULT, description, errors);
            isDefaultSite.ifPresent(siteBuilder::setDefault);
            SmackPacketUtil.getStringAttribute(parser, "type")
                    .flatMap(Site.Type::from)
                    .ifPresent(siteBuilder::setType);
            parser.next();
            final Optional<String> siteName = Optional.ofNullable(parser.getText());
            siteName.ifPresent(siteBuilder::setName);
            return Optional.of(siteBuilder.build(errors));
        } else {
            return Optional.empty();
        }
    }

    public static void addDeviceStatus(@Nonnull IQChildElementXmlStringBuilder xml, @Nonnull final DeviceStatus deviceStatus) {
        final XmlStringBuilder deviceStatusElement = xml.halfOpenElement(ELEMENT_DEVICESTATUS)
                .attribute(ATTRIBUTE_XMLNS, OpenlinkXmppNamespace.OPENLINK_DEVICE_STATUS.uri());
        xml.rightAngleBracket();

        final XmlStringBuilder profileElement = deviceStatusElement.halfOpenElement(ELEMENT_PROFILE);
        deviceStatus.isOnline().ifPresent(online -> profileElement.attribute(ATTRIBUTE_ONLINE, String.valueOf(online)));
        deviceStatus.getDeviceId().ifPresent(deviceId -> profileElement.attribute(ATTRIBUTE_DEVICENUM, String.valueOf(deviceId)));
        deviceStatusElement.rightAngleBracket();
        deviceStatus.getProfileId().ifPresent(profileId -> profileElement.escape(profileId.value()));

        deviceStatusElement.closeElement(ELEMENT_PROFILE);

        final List<VoiceMessageFeature> voiceMessageFeatures = deviceStatus.getFeatures();
        if (!voiceMessageFeatures.isEmpty()) {
            xml.openElement(ELEMENT_FEATURES);

            for (VoiceMessageFeature voiceMessageFeature : voiceMessageFeatures) {
                final XmlStringBuilder featureElement = xml.halfOpenElement(ELEMENT_FEATURE);
                voiceMessageFeature.getId().ifPresent(featureId -> featureElement.attribute(ATTRIBUTE_ID, featureId.value()));
                featureElement.rightAngleBracket();

                voiceMessageFeature.getVoiceMessage()
                        .ifPresent(voiceMessage -> addVoiceMessage(featureElement, voiceMessage));

                xml.closeElement(ELEMENT_FEATURE);
            }

            xml.closeElement(ELEMENT_FEATURES);
        }

        xml.closeElement(ELEMENT_DEVICESTATUS);
    }

    private static void addVoiceMessage(@Nonnull final XmlStringBuilder featureElement, @Nonnull final VoiceMessage voiceMessage) {
        final XmlStringBuilder voiceMessageElement = featureElement.halfOpenElement(ELEMENT_VOICE_MESSAGE)
                .attribute(ATTRIBUTE_XMLNS, OpenlinkXmppNamespace.OPENLINK_VOICE_MESSAGE.uri());
        voiceMessageElement.rightAngleBracket();

        voiceMessage.getLabel().ifPresent(label -> voiceMessageElement.element(ATTRIBUTE_LABEL, label));
        voiceMessage.getStatus().ifPresent(voiceMessageStatus -> voiceMessageElement.element(ELEMENT_STATUS, voiceMessageStatus.getLabel()));
        voiceMessage.getAction().ifPresent(action -> voiceMessageElement.element(ELEMENT_ACTION, action.getId()));
        voiceMessage.getExtension().ifPresent(extension -> voiceMessageElement.element(ELEMENT_EXTENSION, extension.value()));
        voiceMessage.getMsgLength().ifPresent(msgLength -> voiceMessageElement.element(ELEMENT_MESSAGE_LENGTH, formatVoiceMessageLength(msgLength)));

        voiceMessage.getCreationDate().ifPresent(creationDate -> voiceMessageElement.element(ELEMENT_CREATION_DATE, Timestamp.from(creationDate).toString()));

        voiceMessageElement.closeElement(ELEMENT_VOICE_MESSAGE);
    }

    private static String formatVoiceMessageLength(@Nonnull final Duration duration) {
        return Float.toString(duration.toMillis() / 1000f);
    }

    public static void addCallStatus(@Nonnull IQChildElementXmlStringBuilder xml, @Nonnull final CallStatus callStatus) {
        xml.halfOpenElement(ELEMENT_CALLSTATUS)
                .attribute(ATTRIBUTE_XMLNS, "http://xmpp.org/protocol/openlink:01:00:00#call-status");
        callStatus.isCallStatusBusy().ifPresent(callStatusBusy -> xml.attribute("busy", String.valueOf(callStatusBusy)));
        callStatus.getCallForward().ifPresent(callForward -> xml.attribute("fwd", callForward.value()));
        xml.rightAngleBracket();
        for (final Call call : callStatus.getCalls()) {
            xml.openElement("call");
            xml.halfOpenElement(ATTRIBUTE_ID);
            call.getTelephonyCallId().ifPresent(telephonyCallId -> xml.attribute("telephony", telephonyCallId.value()));
            xml.rightAngleBracket();
            call.getId().ifPresent(id -> xml.escape(id.value()));
            xml.closeElement(ATTRIBUTE_ID);
            call.getConferenceId().ifPresent(conferenceId -> xml.element("conference", conferenceId.value()));
            call.getSite().ifPresent(site -> addSiteXML(xml, site));
            call.getProfileId().ifPresent(profileId -> {
                final XmlStringBuilder profileElement = xml.halfOpenElement(ELEMENT_PROFILE);
                call.getDeviceId().ifPresent(deviceId -> profileElement.attribute(ATTRIBUTE_DEVICENUM, deviceId.value()));
                xml.rightAngleBracket();
                profileElement.escape(profileId.value());
                xml.closeElement(ELEMENT_PROFILE);
            });
            call.getUserId().ifPresent(userId -> xml.element("user", userId.value()));
            call.getInterestId().ifPresent(interestId -> xml.element("interest", interestId.value()));
            call.getChanged().ifPresent(changed -> xml.element("changed", changed.getId()));
            call.getState().ifPresent(changed -> xml.element("state", changed.getLabel()));
            call.getDirection().ifPresent(changed -> xml.element(ATTRIBUTE_DIRECTION, changed.getLabel()));
            addCallerDetails(xml, call);
            addCalledDetails(xml, call);
            addOriginatorReferences(xml, call.getOriginatorReferences());
            call.getStartTime().ifPresent(startTime -> xml.element(ATTRIBUTE_START_TIME, (ISO_8601_FORMATTER.format(startTime.atZone(ZoneOffset.UTC)))));
            call.getDuration().ifPresent(duration -> xml.element(ATTRIBUTE_DURATION, String.valueOf(duration.toMillis())));
            addActions(xml, call);
            addFeatures(xml, call);
            addParticipants(xml, call);
            xml.closeElement("call");
        }
        xml.closeElement(ELEMENT_CALLSTATUS);
    }

    private static void addParticipants(@Nonnull final IQChildElementXmlStringBuilder xml, @Nonnull final Call call) {
        final List<Participant> participants = call.getParticipants();
        if (!participants.isEmpty()) {
            xml.openElement(ELEMENT_PARTICIPANTS);
            participants.forEach(participant -> {
                xml.halfOpenElement(ELEMENT_PARTICIPANT);
                participant.getJID().ifPresent(jid -> xml.attribute("jid", jid));
                participant.getNumber().ifPresent(number -> xml.attribute(ATTRIBUTE_NUMBER, number.value()));
                participant.getDestinationNumber().ifPresent(destination -> xml.attribute(ATTRIBUTE_DESTINATION, destination.value()));
                xml.optAttribute("e164Number", joinList(participant.getE164Numbers()));
                participant.getType().ifPresent(type -> xml.attribute("type", type.getId()));
                participant.getParticipantCategory().ifPresent(category -> xml.attribute("category", category.getId()));
                participant.getDirection().ifPresent(direction -> xml.attribute(ATTRIBUTE_DIRECTION, direction.getLabel()));
                participant.getStartTime().ifPresent(startTime -> {
                    final ZonedDateTime startTimeInUTC = startTime.atZone(TimeZone.getTimeZone("UTC").toZoneId());
                    xml.attribute(ATTRIBUTE_START_TIME, ISO_8601_FORMATTER.format(startTimeInUTC));
                    xml.attribute(ATTRIBUTE_TIMESTAMP, JAVA_UTIL_DATE_FORMATTER.format(startTimeInUTC));
                });
                participant.getDuration().ifPresent(duration -> xml.attribute(ATTRIBUTE_DURATION, String.valueOf(duration.toMillis())));
                xml.rightAngleBracket();
                xml.closeElement(ELEMENT_PARTICIPANT);
            });
            xml.closeElement(ELEMENT_PARTICIPANTS);
        }
    }

    private static void addCalledDetails(@Nonnull final IQChildElementXmlStringBuilder xml, @Nonnull final Call call) {
        xml.openElement(ELEMENT_CALLED);
        xml.halfOpenElement(ELEMENT_NUMBER);
        call.getCalledDestination().ifPresent(destination -> xml.attribute(ATTRIBUTE_DESTINATION, destination.value()));
        xml.optAttribute("e164", joinList(call.getCalledE164Numbers()));
        xml.rightAngleBracket();
        call.getCalledNumber().ifPresent(calledNumber -> xml.escape(calledNumber.value()));
        xml.closeElement(ELEMENT_NUMBER);
        call.getCalledName().ifPresent(callerName -> xml.element(ELEMENT_NAME, callerName));
        xml.closeElement(ELEMENT_CALLED);
    }

    @Nullable
    private static String joinList(@Nonnull List<?> numbers) {
        if (numbers.isEmpty()) {
            return null;
        } else {
            return String.join(",", numbers.stream().map(Object::toString).collect(Collectors.toList()));
        }
    }

    private static void addCallerDetails(@Nonnull final IQChildElementXmlStringBuilder xml, @Nonnull final Call call) {
        xml.openElement(ELEMENT_CALLER);
        xml.halfOpenElement(ELEMENT_NUMBER);
        xml.optAttribute("e164", joinList(call.getCallerE164Numbers()));
        xml.rightAngleBracket();
        call.getCallerNumber().ifPresent(callerNumber -> xml.escape(callerNumber.value()));
        xml.closeElement(ELEMENT_NUMBER);
        call.getCallerName().ifPresent(callerName -> xml.element(ELEMENT_NAME, callerName));
        xml.closeElement(ELEMENT_CALLER);
    }

    public static void addOriginatorReferences(@Nonnull final IQChildElementXmlStringBuilder xml, @Nonnull final List<OriginatorReference> originatorReferences) {
        if (!originatorReferences.isEmpty()) {
            xml.openElement(ELEMENT_ORIGINATOR_REF);
            originatorReferences.forEach(originatorReference -> {
                xml.halfOpenElement(ELEMENT_PROPERTY).attribute(ATTRIBUTE_ID, originatorReference.getKey()).rightAngleBracket();
                xml.element("value", originatorReference.getValue());
                xml.closeElement(ELEMENT_PROPERTY);
            });
            xml.closeElement(ELEMENT_ORIGINATOR_REF);
        }
    }

    private static void addActions(@Nonnull final IQChildElementXmlStringBuilder xml, @Nonnull final Call call) {
        final Collection<RequestAction> actions = call.getActions();
        xml.openElement(ELEMENT_ACTIONS);
        if (!actions.isEmpty()) {
            actions.forEach(action -> {
                xml.halfOpenElement(action.getId()).rightAngleBracket();
                xml.closeElement(action.getId());
            });
        }
        xml.closeElement(ELEMENT_ACTIONS);
    }

    public static void addSiteXML(@Nonnull final IQChildElementXmlStringBuilder xml, @Nonnull final Site site) {
        xml.halfOpenElement("site");
        site.getId().ifPresent(id -> xml.attribute(ATTRIBUTE_ID, String.valueOf(id)));
        site.isDefault().ifPresent(isDefault -> xml.attribute(OpenlinkXmppNamespace.TAG_DEFAULT, String.valueOf(isDefault)));
        site.getType().ifPresent(type -> xml.attribute("type", type.getLabel()));
        xml.rightAngleBracket();
        site.getName().ifPresent(xml::escape);
        xml.closeElement("site");
    }

    private static void addFeatures(@Nonnull final IQChildElementXmlStringBuilder xml, @Nonnull final Call call) {
        final List<CallFeature> features = call.getFeatures();
        if (!features.isEmpty()) {
            xml.openElement(ELEMENT_FEATURES);
            features.forEach(feature -> {
                xml.halfOpenElement("feature");
                feature.getId().ifPresent(id -> xml.attribute(ATTRIBUTE_ID, id.value()));
                feature.getType().ifPresent(type -> xml.attribute("type", type.getId()));
                if (feature instanceof CallFeatureBoolean) {
                    feature.getLabel().ifPresent(label -> xml.attribute(ATTRIBUTE_LABEL, label));
                    xml.rightAngleBracket();
                    final CallFeatureBoolean callFeatureBoolean = (CallFeatureBoolean) feature;
                    callFeatureBoolean.isEnabled().ifPresent(enabled -> xml.escape(String.valueOf(enabled)));
                } else if (feature instanceof CallFeatureTextValue) {
                    feature.getLabel().ifPresent(label -> xml.attribute(ATTRIBUTE_LABEL, label));
                    xml.rightAngleBracket();
                    final CallFeatureTextValue callFeatureText = (CallFeatureTextValue) feature;
                    callFeatureText.getValue().ifPresent(enabled -> xml.escape(String.valueOf(enabled)));
                } else if (feature instanceof CallFeatureDeviceKey) {
                    feature.getLabel().ifPresent(label -> xml.attribute(ATTRIBUTE_LABEL, label));
                    xml.rightAngleBracket();
                    final CallFeatureDeviceKey callFeatureDeviceKey = (CallFeatureDeviceKey) feature;
                    xml.halfOpenElement(ELEMENT_DEVICEKEYS);
                    xml.attribute(ATTRIBUTE_XMLNS, OpenlinkXmppNamespace.OPENLINK_DEVICE_KEY.uri());
                    xml.rightAngleBracket();
                    callFeatureDeviceKey.getDeviceKeys().forEach(deviceKey -> xml.element("key", deviceKey.value()));
                    xml.closeElement(ELEMENT_DEVICEKEYS);
                } else if (feature instanceof CallFeatureSpeakerChannel) {
                    xml.rightAngleBracket();
                    final CallFeatureSpeakerChannel callFeatureSpeakerChannel = (CallFeatureSpeakerChannel) feature;
                    xml.halfOpenElement(ELEMENT_SPEAKERCHANNEL);
                    xml.attribute(ATTRIBUTE_XMLNS, OpenlinkXmppNamespace.OPENLINK_SPEAKER_CHANNEL.uri());
                    xml.rightAngleBracket();
                    callFeatureSpeakerChannel.getChannel().ifPresent(channel -> xml.element(ELEMENT_CHANNEL, String.valueOf(channel)));
                    callFeatureSpeakerChannel.isMicrophoneActive().ifPresent(microphone -> xml.element(ELEMENT_MICROPHONE, String.valueOf(microphone)));
                    callFeatureSpeakerChannel.isMuteRequested().ifPresent(muteRequested -> xml.element(ELEMENT_MUTE, String.valueOf(muteRequested)));
                    xml.closeElement(ELEMENT_SPEAKERCHANNEL);
                } else if (feature instanceof CallFeatureVoiceRecorder) {
                    xml.rightAngleBracket();
                    final CallFeatureVoiceRecorder callFeatureVoiceRecorder = (CallFeatureVoiceRecorder) feature;
                    xml.halfOpenElement(ELEMENT_VOICERECORDER);
                    xml.attribute(ATTRIBUTE_XMLNS, OpenlinkXmppNamespace.OPENLINK_VOICE_RECORDER.uri());
                    xml.rightAngleBracket();

                    callFeatureVoiceRecorder.getVoiceRecorderInfo()
                            .ifPresent(voiceRecorderInfo -> {
                                voiceRecorderInfo.getRecorderNumber().ifPresent(recorderNumber -> xml.element(ELEMENT_RECORDER_NUMBER, recorderNumber.value()));
                                voiceRecorderInfo.getRecorderPort().ifPresent(recorderPort -> xml.element(ELEMENT_RECORDER_PORT, recorderPort.value()));
                                voiceRecorderInfo.getRecorderChannel().ifPresent(recorderChannel -> xml.element(ELEMENT_RECORDER_CHANNEL, recorderChannel.value()));
                                voiceRecorderInfo.getRecorderType().ifPresent(recorderType -> xml.element(ELEMENT_RECORDER_TYPE, recorderType.value()));
                            });

                    xml.closeElement(ELEMENT_VOICERECORDER);
                } else {
                    feature.getLabel().ifPresent(label -> xml.attribute(ATTRIBUTE_LABEL, label));
                    xml.rightAngleBracket();
                }
                xml.closeElement("feature");
            });
            xml.closeElement(ELEMENT_FEATURES);
        }
    }

    @SuppressWarnings("unchecked")
    public static Optional<DeviceStatus> getDeviceStatus(
            @Nonnull final XmlPullParser parser,
            @Nonnull final List<String> errors) throws IOException, XmlPullParserException {

        final DeviceStatus.Builder deviceStatusBuilder = DeviceStatus.Builder.start();

        if (!parser.getName().equals(ELEMENT_DEVICESTATUS)) {
            return Optional.empty();
        }

        getBooleanAttribute(parser, ATTRIBUTE_ONLINE, ATTRIBUTE_ONLINE, errors).ifPresent(deviceStatusBuilder::setOnline);
        getStringAttribute(parser, ATTRIBUTE_DEVICENUM).flatMap(DeviceId::from).ifPresent(deviceStatusBuilder::setDeviceId);

        final int inDepth = parser.getDepth();
        parser.nextTag();
        while (parser.getDepth() > inDepth) {
            switch (parser.getName()) {
            case ELEMENT_PROFILE:
                getElementTextString(parser).flatMap(ProfileId::from).ifPresent(deviceStatusBuilder::setProfileId);
                break;
            case ELEMENT_FEATURES:
                addDeviceStatusFeaturesToBuilder(parser, deviceStatusBuilder, errors);
                break;
            case ATTRIBUTE_ID:
                //ignore
                break;
            default:
                errors.add("Unrecognised tag: " + parser.getName());
                break;
            }

            ParserUtils.forwardToEndTagOfDepth(parser, inDepth + 1);
            parser.nextTag();
        }
        return Optional.of(deviceStatusBuilder.build(errors));
    }

    private static void addDeviceStatusFeaturesToBuilder(
            @Nonnull final XmlPullParser parser,
            @Nonnull final DeviceStatus.Builder deviceStatusBuilder,
            @Nonnull final List<String> errors) throws IOException, XmlPullParserException {

        parser.nextTag();
        while (parser.getName().equals(ELEMENT_FEATURE)) {
            final VoiceMessageFeature.Builder voiceMessageFeatureBuilder = VoiceMessageFeature.Builder.start();

            getStringAttribute(parser, ATTRIBUTE_ID)
                    .flatMap(FeatureId::from)
                    .ifPresent(voiceMessageFeatureBuilder::setId);

            final int callDepth = parser.getDepth();
            parser.nextTag();
            do {
                switch (parser.getName()) {
                case ELEMENT_VOICE_MESSAGE:
                    addVoiceMessagesToBuilder(parser, voiceMessageFeatureBuilder, errors);
                    break;
                default:
                    errors.add("Unrecognised tag: " + parser.getName());
                    break;
                }

                deviceStatusBuilder.addFeature(voiceMessageFeatureBuilder.build(errors));
            } while (callDepth != parser.getDepth());
            parser.nextTag();
        }
    }

    private static void addVoiceMessagesToBuilder(
            @Nonnull final XmlPullParser parser,
            @Nonnull final VoiceMessageFeature.Builder builder,
            @Nonnull final List<String> errors) throws IOException, XmlPullParserException {
        while (parser.getName().equals(ELEMENT_VOICE_MESSAGE)) {
            final int callDepth = parser.getDepth();
            parser.nextTag();

            final VoiceMessage.Builder voiceMessageBuilder = VoiceMessage.Builder.start();

            do {
                switch (parser.getName()) {
                case ATTRIBUTE_LABEL:
                    getElementTextString(parser)
                            .ifPresent(voiceMessageBuilder::setLabel);
                    break;
                case ELEMENT_STATUS:
                    getElementTextString(parser)
                            .flatMap(VoiceMessageStatus::from)
                            .ifPresent(voiceMessageBuilder::setStatus);
                    break;
                case ELEMENT_ACTION:
                    getElementTextString(parser)
                            .flatMap(ManageVoiceMessageAction::from)
                            .ifPresent(voiceMessageBuilder::setAction);
                    break;
                case ELEMENT_MESSAGE_LENGTH:
                    getElementTextString(parser)
                            .flatMap(msgLength -> {
                                try {
                                    return Optional.of(Duration.ofMillis((long) (Float.parseFloat(msgLength) * 1000)));
                                } catch (Exception e) {
                                    return Optional.empty();
                                }
                            })
                            .ifPresent(voiceMessageBuilder::setMsgLength);

                    break;
                case ELEMENT_CREATION_DATE:
                    getElementTextString(parser)
                            .flatMap(msgLength -> {
                                try {
                                    return Optional.of(Timestamp.valueOf(msgLength).toInstant());
                                } catch (Exception e) {
                                    return Optional.empty();
                                }
                            })
                            .ifPresent(voiceMessageBuilder::setCreationDate);
                    break;
                case ELEMENT_EXTENSION:
                    getElementTextString(parser)
                            .flatMap(PhoneNumber::from)
                            .ifPresent(voiceMessageBuilder::setExtension);

                    break;
                default:
                    errors.add("Unrecognised tag: " + parser.getName());
                    break;
                }
                ParserUtils.forwardToEndTagOfDepth(parser, callDepth + 1);
                parser.nextTag();
            } while (callDepth != parser.getDepth());

            builder.setVoiceMessage(voiceMessageBuilder.build(errors));

            parser.nextTag();
        }
    }

    @SuppressWarnings("unchecked")
    public static Optional<CallStatus> getCallStatus(
            @Nonnull final XmlPullParser parser,
            @Nonnull final String description,
            @Nonnull final List<String> errors)
            throws IOException, XmlPullParserException {
        if (!parser.getName().equals(ELEMENT_CALLSTATUS)) {
            return Optional.empty();
        }
        final CallStatus.Builder builder = CallStatus.Builder.start();
        getBooleanAttribute(parser, "busy", description, errors).ifPresent(builder::setCallStatusBusy);
        getStringAttribute(parser, "fwd").flatMap(PhoneNumber::from).ifPresent(builder::setCallForward);
        parser.nextTag();

        while (parser.getName().equals("call")) {
            final Call.Builder callBuilder = Call.Builder.start();
            final int callDepth = parser.getDepth();
            parser.nextTag();
            do {
                switch (parser.getName()) {
                case ATTRIBUTE_ID:
                    addCallIdToBuilder(parser, callBuilder);
                    break;
                case "conference":
                    addConferenceIdToBuilder(parser, callBuilder);
                    break;
                case "site":
                    addSiteToBuilder(parser, errors, callBuilder, description);
                    break;
                case ELEMENT_PROFILE:
                    addProfileIdToBuilder(parser, callBuilder);
                    break;
                case "user":
                    addUserToBuilder(parser, callBuilder);
                    break;
                case "interest":
                    addInterestToBuilder(parser, callBuilder);
                    break;
                case "changed":
                    addChangedToBuilder(parser, callBuilder);
                    break;
                case "state":
                    addCallStateToBuilder(parser, callBuilder);
                    break;
                case ATTRIBUTE_DIRECTION:
                    addDirectionToBuilder(parser, callBuilder);
                    break;
                case ELEMENT_CALLER:
                    addCallerDetailsToBuilder(parser, callBuilder);
                    break;
                case ELEMENT_CALLED:
                    addCalledDetailsToBuilder(parser, callBuilder);
                    break;
                case ELEMENT_ORIGINATOR_REF:
                    getOriginatorRefs(parser).forEach(callBuilder::addOriginatorReference);
                    break;
                case ATTRIBUTE_START_TIME:
                    getElementTestISO8601(ATTRIBUTE_START_TIME, parser, description, errors).ifPresent(callBuilder::setStartTime);
                    break;
                case ATTRIBUTE_DURATION:
                    getElementTextLong(ATTRIBUTE_DURATION, parser, description, errors).map(Duration::ofMillis).ifPresent(callBuilder::setDuration);
                    break;
                case ELEMENT_ACTIONS:
                    getActions(callBuilder, parser, errors);
                    break;
                case ELEMENT_FEATURES:
                    getFeatures(callBuilder, parser, description, errors);
                    break;
                case ELEMENT_PARTICIPANTS:
                    getParticipants(callBuilder, parser, description, errors);
                    break;
                default:
                    errors.add("Unrecognised tag: " + parser.getName());
                    break;
                }
                ParserUtils.forwardToEndTagOfDepth(parser, callDepth + 1);
                parser.nextTag();
            } while (callDepth != parser.getDepth());
            builder.addCall(callBuilder.build(errors));

            parser.nextTag();
        }

        return Optional.of(builder.build(errors));
    }

    public static List<OriginatorReference> getOriginatorRefs(final @Nonnull XmlPullParser parser) throws XmlPullParserException, IOException {
        final List<OriginatorReference> originatorReferences = new ArrayList<>();
        parser.nextTag();
        while (parser.getName().equals(ELEMENT_PROPERTY)) {
            final int propertyDepth = parser.getDepth();
            final String key = SmackPacketUtil.getStringAttribute(parser, ATTRIBUTE_ID).orElse("");
            parser.nextTag();
            final String value;
            if (parser.getName().equals("value")) {
                value = parser.nextText();
            } else {
                value = "";
            }
            originatorReferences.add(new OriginatorReference(key, value));
            ParserUtils.forwardToEndTagOfDepth(parser, propertyDepth);
            parser.nextTag(); // end of property tag
        }
        return originatorReferences;
    }

    private static void addCalledDetailsToBuilder(final @Nonnull XmlPullParser parser, final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        parser.nextTag();
        if (parser.getName().equals(ELEMENT_NUMBER)) {
            callBuilder.addCalledE164Numbers(getPhoneNumbers(parser, "e164"));
            SmackPacketUtil.getStringAttribute(parser, ATTRIBUTE_DESTINATION)
                    .flatMap(PhoneNumber::from)
                    .ifPresent(callBuilder::setCalledDestination);
            final String calledNumberString = parser.nextText();
            final Optional<PhoneNumber> calledNumberOptional = PhoneNumber.from(calledNumberString);
            calledNumberOptional.ifPresent(callBuilder::setCalledNumber);
            parser.nextTag();
        }
        if (parser.getName().equals(ELEMENT_NAME)) {
            final Optional<String> callerName = Optional.ofNullable(parser.nextText());
            callerName.ifPresent(callBuilder::setCalledName);
            parser.nextTag(); // moves to end of caller tag
        }
    }

    private static void addCallerDetailsToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        parser.nextTag();
        if (parser.getName().equals(ELEMENT_NUMBER)) {
            callBuilder.addCallerE164Numbers(getPhoneNumbers(parser, "e164"));
            final String callerNumberString = parser.nextText();
            final Optional<PhoneNumber> callerNumberOptional = PhoneNumber.from(callerNumberString);
            callerNumberOptional.ifPresent(callBuilder::setCallerNumber);
            parser.nextTag();
        }
        if (parser.getName().equals(ELEMENT_NAME)) {
            final Optional<String> callerName = Optional.ofNullable(parser.nextText());
            callerName.ifPresent(callBuilder::setCallerName);
            parser.nextTag(); // moves to end of caller tag
        }
    }

    private static void addDirectionToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        final String callDirectionString = parser.nextText();
        final Optional<CallDirection> callDirectionOptional = CallDirection.from(callDirectionString);
        callDirectionOptional.ifPresent(callBuilder::setDirection);
    }

    private static void addCallStateToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        final String callStateString = parser.nextText();
        final Optional<CallState> callStateOptional = CallState.from(callStateString);
        callStateOptional.ifPresent(callBuilder::setState);
    }

    private static void addChangedToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        final String changedString = parser.nextText();
        final Optional<Changed> changedOptional = Changed.from(changedString);
        changedOptional.ifPresent(callBuilder::setChanged);
    }

    private static void addInterestToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        final String interestIdString = parser.nextText();
        final Optional<InterestId> interestIdOptional = InterestId.from(interestIdString);
        interestIdOptional.ifPresent(callBuilder::setInterestId);
    }

    private static void addUserToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        final String userIdString = parser.nextText();
        final Optional<UserId> userIdOptional = UserId.from(userIdString);
        userIdOptional.ifPresent(callBuilder::setUserId);
    }

    private static void addProfileIdToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        getStringAttribute(parser, ATTRIBUTE_DEVICENUM).flatMap(DeviceId::from).ifPresent(callBuilder::setDeviceId);

        final String profileIdString = parser.nextText();
        final Optional<ProfileId> profileIdOptional = ProfileId.from(profileIdString);
        profileIdOptional.ifPresent(callBuilder::setProfileId);
    }

    private static void addSiteToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final List<String> errors, final Call.Builder callBuilder, final String description) throws IOException, XmlPullParserException {
        Optional<Site> site = getSite(parser, errors, description);
        site.ifPresent(callBuilder::setSite);
    }

    private static void addConferenceIdToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        final String conferenceIdString = parser.nextText();
        final Optional<ConferenceId> conferenceIdOptional = ConferenceId.from(conferenceIdString);
        conferenceIdOptional.ifPresent(callBuilder::setConferenceId);
    }

    private static void addCallIdToBuilder(@Nonnull final XmlPullParser parser, @Nonnull final Call.Builder callBuilder) throws XmlPullParserException, IOException {
        final String telephonyCallId = parser.getAttributeValue("", "telephony");
        TelephonyCallId.from(telephonyCallId).ifPresent(callBuilder::setTelephonyCallId);
        final String callIdString = parser.nextText();
        CallId.from(callIdString).ifPresent(callBuilder::setId);
    }

    private static List<PhoneNumber> getPhoneNumbers(final XmlPullParser parser, String attributeName) {
        final Optional<String> e164String = SmackPacketUtil.getStringAttribute(parser, attributeName);
        final List<PhoneNumber> phoneNumbers = new ArrayList<>();
        e164String.ifPresent(string -> Arrays.stream(string.split(",")).map(String::trim).map(PhoneNumber::from)
                .filter(Optional::isPresent).map(Optional::get).forEach(phoneNumbers::add));
        return phoneNumbers;
    }

    @SuppressWarnings("unchecked")
    private static void getParticipants(
            @Nonnull final Call.Builder callBuilder,
            @Nonnull final XmlPullParser parser,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors) throws IOException, XmlPullParserException {

        if (parser.getName().equals(ELEMENT_PARTICIPANTS)) {
            parser.nextTag();
            while (parser.getName().equals(ELEMENT_PARTICIPANT)) {
                final Participant.Builder participantBuilder = Participant.Builder.start();
                SmackPacketUtil.getStringAttribute(parser, "jid").ifPresent(participantBuilder::setJID);
                SmackPacketUtil.getStringAttribute(parser, ATTRIBUTE_NUMBER).flatMap(PhoneNumber::from).ifPresent(participantBuilder::setNumber);
                SmackPacketUtil.getStringAttribute(parser, ATTRIBUTE_DESTINATION).flatMap(PhoneNumber::from).ifPresent(participantBuilder::setDestinationNumber);
                participantBuilder.addE164Numbers(getPhoneNumbers(parser, "e164Number"));
                SmackPacketUtil.getStringAttribute(parser, "type")
                        .flatMap(ParticipantType::from)
                        .ifPresent(participantBuilder::setType);
                SmackPacketUtil.getStringAttribute(parser, "category")
                        .flatMap(ParticipantCategory::from)
                        .ifPresent(participantBuilder::setParticipantCategory);
                SmackPacketUtil.getStringAttribute(parser, ATTRIBUTE_DIRECTION)
                        .flatMap(CallDirection::from)
                        .ifPresent(participantBuilder::setDirection);
                final Optional<Instant> participantTimestamp = getJavaUtilDateAttribute(parser, ATTRIBUTE_TIMESTAMP, description, parseErrors);
                participantTimestamp.ifPresent(participantBuilder::setStartTime);
                final Optional<Instant> participantStartTime = getISO8601Attribute(parser, ATTRIBUTE_START_TIME, description, parseErrors);
                participantStartTime.ifPresent(participantBuilder::setStartTime);
                if (participantStartTime.isPresent() && participantTimestamp.isPresent()
                        && !participantStartTime.equals(participantTimestamp)) {
                    parseErrors.add("Invalid participant; the legacy timestamp field does not match the start time field");
                }
                final Optional<Long> participantDuration = SmackPacketUtil.getLongAttribute(parser, ATTRIBUTE_DURATION);
                participantDuration.ifPresent(millis -> participantBuilder.setDuration(Duration.ofMillis(millis)));
                callBuilder.addParticipant(participantBuilder.build(parseErrors));
                ParserUtils.forwardToEndTagOfDepth(parser, parser.getDepth());
                parser.nextTag();
            }
        }
    }

    private static Optional<Instant> getJavaUtilDateAttribute(
            final XmlPullParser parser,
            final String attributeName,
            final String description,
            final List<String> parseErrors) {
        final Optional<String> stringValue = SmackPacketUtil.getStringAttribute(parser, attributeName);
        try {
            return stringValue.map(string -> Instant.from(JAVA_UTIL_DATE_FORMATTER.parse(string)));
        } catch (final DateTimeParseException ignored) {
            parseErrors.add(String.format("Invalid %s; invalid %s '%s'; format should be 'dow mon dd hh:mm:ss zzz yyyy'", description, attributeName, stringValue));
            return Optional.empty();
        }
    }

    private static Optional<Instant> getISO8601Attribute(
            @Nonnull final XmlPullParser parser,
            @Nonnull final String attributeName,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors) {

        final Optional<String> stringValue = SmackPacketUtil.getStringAttribute(parser, attributeName);
        try {
            return stringValue.map(Instant::parse);
        } catch (final DateTimeParseException ignored) {
            parseErrors.add(String.format("Invalid %s; invalid %s '%s'; format should be compliant with XEP-0082", description, attributeName, stringValue));
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static void getActions(@Nonnull final Call.Builder callBuilder, final XmlPullParser parser,
            @Nonnull final List<String> parseErrors) throws IOException, XmlPullParserException {
        if (parser.getName().equals(ELEMENT_ACTIONS)) {
            final int actionsDepth = parser.getDepth();
            parser.nextTag();
            while (parser.getDepth() > actionsDepth) {
                final Optional<RequestAction> requestAction = RequestAction.from(parser.getName());
                if (requestAction.isPresent()) {
                    callBuilder.addAction(requestAction.get());
                } else {
                    parseErrors.add("Invalid %s: %s is not a valid action");
                }
                ParserUtils.forwardToEndTagOfDepth(parser, parser.getDepth());
                parser.nextTag();
            }
            ParserUtils.forwardToEndTagOfDepth(parser, actionsDepth);
        }
    }

    @SuppressWarnings("unchecked")
    private static void getFeatures(
            @Nonnull final Call.Builder callBuilder,
            @Nonnull final XmlPullParser parser,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors) throws IOException, XmlPullParserException {

        if (parser.getName().equals(ELEMENT_FEATURES)) {
            final int featuresDepth = parser.getDepth();
            parser.nextTag();
            while (OpenlinkXmppNamespace.TAG_FEATURE.equals(parser.getName())) {
                final Optional<FeatureId> featureId = FeatureId.from(parser.getAttributeValue("", ATTRIBUTE_ID));
                final Optional<FeatureType> optFeatureType = SmackPacketUtil.getStringAttribute(parser, "type")
                        .flatMap(featureType -> {
                            final Optional<FeatureType> type = FeatureType.from(featureType);
                            if (!type.isPresent()) {
                                parseErrors.add("Invalid %s; invalid feature type - '%s'");
                            }
                            return type;
                        });

                final Optional<String> label = SmackPacketUtil.getStringAttribute(parser, OpenlinkXmppNamespace.TAG_LABEL);
                String text = "";
                while (parser.next() == XmlPullParser.TEXT) {
                    text = parser.getText();
                }
                final CallFeature.AbstractCallFeatureBuilder callFeatureBuilder = getCallFeatureBuilder(parser, optFeatureType.orElse(null), description, parseErrors, text);
                featureId.ifPresent(callFeatureBuilder::setId);
                label.ifPresent(callFeatureBuilder::setLabel);
                optFeatureType.ifPresent(callFeatureBuilder::setType);

                callBuilder.addFeature(callFeatureBuilder.build(parseErrors));
            }
            ParserUtils.forwardToEndTagOfDepth(parser, featuresDepth);
        }
    }

    @Nonnull
    private static CallFeature.AbstractCallFeatureBuilder getCallFeatureBuilder(
            @Nonnull final XmlPullParser parser,
            @Nullable FeatureType featureType,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors,
            @Nonnull final String text)
            throws XmlPullParserException, IOException {
        final CallFeature.AbstractCallFeatureBuilder callFeatureBuilder;
        if (parser.getEventType() == XmlPullParser.START_TAG) {
            switch (parser.getName()) {
            case ELEMENT_DEVICEKEYS:
                callFeatureBuilder = getDeviceKeyFeatureBuilder(parser);
                break;
            case ELEMENT_SPEAKERCHANNEL:
                callFeatureBuilder = getSpeakerChannelFeatureBuilder(parser, description, parseErrors);
                break;
            case ELEMENT_VOICERECORDER:
                callFeatureBuilder = getVoiceRecorderFeatureBuilder(parser, parseErrors);
                break;
            default:
                // Assume a simple true/false feature
                callFeatureBuilder = getBooleanFeatureBuilder(text, description, parseErrors, parser.getName());
                break;
            }
        } else if (FeatureType.VOICE_MESSAGE.equals(featureType)) {
            callFeatureBuilder = getTextFeatureBuilder(text);
            parser.nextTag();
        } else {
            // Assume a simple true/false feature
            callFeatureBuilder = getBooleanFeatureBuilder(text, description, parseErrors, parser.getName());
            parser.nextTag();
        }
        return callFeatureBuilder;
    }

    private static CallFeature.AbstractCallFeatureBuilder getVoiceRecorderFeatureBuilder(
            @Nonnull final XmlPullParser parser,
            @Nonnull final List<String> parseErrors) throws XmlPullParserException, IOException {
        CallFeature.AbstractCallFeatureBuilder callFeatureBuilder;
        final CallFeatureVoiceRecorder.Builder voiceRecorderBuilder = CallFeatureVoiceRecorder.Builder.start();
        final VoiceRecorderInfo.Builder voiceRecorderInfoBuilder = VoiceRecorderInfo.Builder.start();

        final int featureDepth = parser.getDepth();
        parser.nextTag();
        while (parser.getDepth() > featureDepth) {
            switch (parser.getName()) {
            case ELEMENT_RECORDER_NUMBER:
                getElementTextString(parser).flatMap(RecorderNumber::from).ifPresent(voiceRecorderInfoBuilder::setRecorderNumber);
                break;
            case ELEMENT_RECORDER_CHANNEL:
                getElementTextString(parser).flatMap(RecorderChannel::from).ifPresent(voiceRecorderInfoBuilder::setRecorderChannel);
                break;
            case ELEMENT_RECORDER_PORT:
                getElementTextString(parser).flatMap(RecorderPort::from).ifPresent(voiceRecorderInfoBuilder::setRecorderPort);
                break;
            case ELEMENT_RECORDER_TYPE:
                getElementTextString(parser).flatMap(RecorderType::from).ifPresent(voiceRecorderInfoBuilder::setRecorderType);
                break;
            default:
                parseErrors.add("Unrecognised element:" + parser.getName());
                break;
            }
            ParserUtils.forwardToEndTagOfDepth(parser, featureDepth + 1);
            parser.nextTag();
        }
        voiceRecorderBuilder.setVoiceRecorderInfo(voiceRecorderInfoBuilder.build(parseErrors));
        callFeatureBuilder = voiceRecorderBuilder;
        return callFeatureBuilder;
    }

    @Nonnull
    private static CallFeature.AbstractCallFeatureBuilder getTextFeatureBuilder(
            @Nullable final String text) {
        final CallFeatureTextValue.Builder textValueBuiler = CallFeatureTextValue.Builder.start();
        textValueBuiler.setValue(text);
        return textValueBuiler;
    }

    @Nonnull
    private static CallFeature.AbstractCallFeatureBuilder getBooleanFeatureBuilder(
            @Nullable final String text,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors,
            @Nonnull final String elementName) {
        final CallFeatureBoolean.Builder booleanBuilder = CallFeatureBoolean.Builder.start();
        getBoolean(text, elementName, description, parseErrors).ifPresent(booleanBuilder::setEnabled);
        return booleanBuilder;
    }

    @Nonnull
    private static CallFeature.AbstractCallFeatureBuilder getSpeakerChannelFeatureBuilder(
            @Nonnull final XmlPullParser parser,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors)
            throws XmlPullParserException, IOException {
        final CallFeatureSpeakerChannel.Builder speakerChannelBuilder = CallFeatureSpeakerChannel.Builder.start();
        final int featureDepth = parser.getDepth() - 1;
        final int speakerChannelDepth = parser.getDepth();
        parser.nextTag();
        while (parser.getDepth() > speakerChannelDepth) {
            switch (parser.getName()) {
            case ELEMENT_CHANNEL:
                getElementTextLong(ELEMENT_CHANNEL, parser, description, parseErrors).ifPresent(speakerChannelBuilder::setChannel);
                break;
            case ELEMENT_MICROPHONE:
                getElementTextBoolean(ELEMENT_MICROPHONE, parser, description, parseErrors).ifPresent(speakerChannelBuilder::setMicrophoneActive);
                break;
            case ELEMENT_MUTE:
                getElementTextBoolean(ELEMENT_MUTE, parser, description, parseErrors).ifPresent(speakerChannelBuilder::setMuteRequested);
                break;
            default:
                parseErrors.add("Unrecognised element:" + parser.getName());
                break;
            }
            ParserUtils.forwardToEndTagOfDepth(parser, speakerChannelDepth + 1);
            parser.nextTag();
        }
        ParserUtils.forwardToEndTagOfDepth(parser, featureDepth);
        parser.nextTag();
        return speakerChannelBuilder;
    }

    @Nonnull
    private static CallFeature.AbstractCallFeatureBuilder getDeviceKeyFeatureBuilder(@Nonnull final XmlPullParser parser) throws XmlPullParserException, IOException {
        final int featureDepth = parser.getDepth() - 1;
        final CallFeatureDeviceKey.Builder deviceKeyBuilder = CallFeatureDeviceKey.Builder.start();
        if (parser.nextTag() == XmlPullParser.START_TAG) {
            while ("key".equalsIgnoreCase(parser.getName())) {
                DeviceKey.from(parser.nextText()).ifPresent(deviceKeyBuilder::addDeviceKey);
                parser.nextTag();
            }
        }
        ParserUtils.forwardToEndTagOfDepth(parser, featureDepth);
        parser.nextTag();
        return deviceKeyBuilder;
    }

    private static Optional<Boolean> getBoolean(String booleanText, final String childElementName, final String description, final List<String> parseErrors) {
        if ("true".equals(booleanText)) {
            return Optional.of(Boolean.TRUE);
        } else if ("false".equals(booleanText)) {
            return Optional.of(Boolean.FALSE);
        } else {
            parseErrors.add(String.format("Invalid %s; invalid %s '%s'; please supply an integer", description, childElementName, booleanText));
            return Optional.empty();
        }
    }

    @Nonnull
    private static Optional<Instant> getElementTestISO8601(
            @Nonnull final String childElementName,
            @Nonnull final XmlPullParser parser,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors)
            throws XmlPullParserException, IOException {
        if (parser.getName().equals(childElementName)) {
            final String childElementText = parser.nextText();
            try {
                return Optional.of(Instant.parse(childElementText));
            } catch (final DateTimeParseException ignored) {
                parseErrors.add(String.format("Invalid %s; invalid %s '%s'; format should be compliant with XEP-0082", description, childElementName, childElementText));
            }
        }
        return Optional.empty();
    }

    @Nonnull
    private static Optional<Long> getElementTextLong(
            @Nonnull final String childElementName,
            @Nonnull final XmlPullParser parser,
            @Nonnull final String stanzaDescription,
            @Nonnull final List<String> parseErrors)
            throws XmlPullParserException, IOException {
        final String childElementText = parser.nextText();
        try {
            return Optional.of(Long.parseLong(childElementText));
        } catch (final NumberFormatException ignored) {
            parseErrors.add(String.format("Invalid %s; invalid %s '%s'; please supply an integer", stanzaDescription, childElementName, childElementText));
        }
        return Optional.empty();
    }

    @Nonnull
    private static Optional<Boolean> getElementTextBoolean(
            @Nonnull final String childElementName,
            @Nonnull final XmlPullParser parser,
            @Nonnull final String description,
            @Nonnull final List<String> parseErrors) throws XmlPullParserException, IOException {
        final String value = parser.nextText();
        return getBoolean(value, childElementName, description, parseErrors);
    }

    @Nonnull
    public static Optional<String> getElementTextString(@Nonnull final XmlPullParser parser) throws IOException, XmlPullParserException {
        final String value = parser.nextText();
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
    }
}
