package com.bt.openlink.tinder.message;

import java.util.List;

import javax.annotation.Nonnull;

import org.dom4j.Element;
import org.xmpp.packet.Message;

import com.bt.openlink.tinder.internal.TinderPacketUtil;

public final class OpenlinkMessageParser {

    private OpenlinkMessageParser() {
    }

    @Nonnull
    public static Message parse(@Nonnull final Message message) {

        final List elements = message.getElement().elements();
        if (elements.isEmpty()) {
            return message;
        }
        final Element childElement = (Element) elements.get(0);
        final String namespace = childElement.getNamespaceURI();
        if (namespace.equals("http://jabber.org/protocol/pubsub#event")) {
            return parsePubSubEvent(message);
        } else {
            return message;
        }
    }

    @Nonnull
    private static Message parsePubSubEvent(final Message message) {
        final Element callStatusElement = TinderPacketUtil.getChildElement(message.getElement(), "event", "items", "item", "callstatus");
        final Element deviceStatusElement = TinderPacketUtil.getChildElement(message.getElement(), "event", "items", "item", "devicestatus");
        if (callStatusElement != null) {
            return CallStatusMessage.from(message);
        } else if (deviceStatusElement != null) {
            return DeviceStatusMessage.from(message);
        } else {
            return message;
        }
    }
}
