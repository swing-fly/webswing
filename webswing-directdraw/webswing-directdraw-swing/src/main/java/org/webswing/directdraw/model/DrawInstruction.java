package org.webswing.directdraw.model;

import java.awt.image.*;
import java.util.*;

import org.webswing.directdraw.*;
import org.webswing.directdraw.proto.Directdraw.*;
import org.webswing.directdraw.proto.Directdraw.DrawInstructionProto.*;
import org.webswing.directdraw.toolkit.*;

public class DrawInstruction implements Iterable<DrawConstant> {

	private final InstructionProto instruction;
	private final DrawConstant[] args;
	private final WebImage image;

	public DrawInstruction(InstructionProto instruction, DrawConstant... args) {
		this.instruction = instruction;
		this.args = args;
        this.image = null;
	}

	public DrawInstruction(WebImage image, DrawConstant... args) {
		instruction = InstructionProto.DRAW_WEBIMAGE;
		this.image = image;
		this.args = args;
	}

	public DrawConstant getArg(int index) {
		return args[index];
	}

	public BufferedImage getImage() {
		return image.getSnapshot();
	}

	public InstructionProto getInstruction() {
		return instruction;
	}

	public DrawInstructionProto toMessage(DirectDraw dd) {
		DrawInstructionProto.Builder builder = DrawInstructionProto.newBuilder();
		builder.setInst(instruction);
		for (DrawConstant c : args) {
			builder.addArgs(c.getId());
		}
		if (image != null) {
			builder.setWebImage(image.toMessage(dd).toByteString());
		}
		return builder.build();
	}

    @Override
    public Iterator<DrawConstant> iterator() {
        return new Iterator<DrawConstant>()
        {
            int index;
            
            @Override
            public boolean hasNext() {
                return index < args.length;
            }

            @Override
            public DrawConstant next() {
                return args[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    @Override
	public String toString() {
		return instruction.name();
	}
}
