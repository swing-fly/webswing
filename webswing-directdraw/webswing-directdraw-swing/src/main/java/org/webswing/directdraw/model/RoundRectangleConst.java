package org.webswing.directdraw.model;

import java.awt.geom.RoundRectangle2D;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.proto.Directdraw.RoundRectangleProto;

public class RoundRectangleConst extends DrawConstant {

	public RoundRectangleConst(DirectDraw context, RoundRectangle2D r) {
		super(context);
		RoundRectangleProto.Builder model = RoundRectangleProto.newBuilder();
		model.setX((int) r.getX());
		model.setY((int) r.getY());
		model.setW((int) r.getWidth());
		model.setH((int) r.getHeight());
		model.setArcH((int) r.getArcHeight());
		model.setArcW((int) r.getArcWidth());
		this.message = model.build();
	}

	@Override
	public String getFieldName() {
		return "roundRectangle";
	}

	public RoundRectangle2D.Float getRoundRectangle(boolean biased) {
		RoundRectangleProto rp = (RoundRectangleProto) message;
		float bias = biased ? 0.5f : 0f;
		return new RoundRectangle2D.Float(rp.getX() + bias, rp.getY() + bias, rp.getW(), rp.getH(), rp.getArcW(), rp.getArcH());
	}
}