package org.webswing.model.s2c;

import org.webswing.model.MsgInternal;

public class PrinterJobResultMsgInternal implements MsgInternal {

	private static final long serialVersionUID = 6352518694214860256L;
	private byte[] pdf;
	private String id;
	private String clientId;

	public byte[] getPdf() {
		return pdf;
	}

	public void setPdf(byte[] pdf) {
		this.pdf = pdf;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

}