package destiny.penumbra_phantasm.client.render.textbox;

import net.minecraft.network.chat.Component;

public final class TextBoxLine {
	public final Component text;
	public final Runnable onBegin;
	public final Runnable onEnd;
	public char waitAfterChar;
	public float waitTicks;

	public TextBoxLine(Component text, Runnable onBegin, Runnable onEnd) {
		this.text = text;
		this.onBegin = onBegin;
		this.onEnd = onEnd;
	}

	public TextBoxLine(Component text) {
		this.text = text;
		this.onBegin = null;
		this.onEnd = null;
	}
}
