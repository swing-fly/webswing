package org.webswing.model.s2c;

import org.webswing.model.Msg;

public enum SimpleEventMsgOut implements Msg {
	applicationAlreadyRunning, shutDownNotification, tooManyClientsNotification, continueOldSession, configurationError;

	public AppFrameMsgOut buildMsgOut() {
		AppFrameMsgOut result = new AppFrameMsgOut();
		result.event = this;
		return result;
	}
}