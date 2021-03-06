
package net.imglib2.labkit.panel;

import net.imglib2.labkit.models.Holder;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

// TODO use Tims CardPanel https://raw.githubusercontent.com/knime-ip/knip-bdv/4489ea811ce5155038ec919c708ed8b84a6b0297/org.knime.knip.bdv.panel/src/org/knime/knip/bdv/uicomponents/CardPanel.java
public class GuiUtils {

	private GuiUtils() {
		// prevent from instantiation
	}

	public static JButton createActionIconButton(String name, final Action action,
		String icon)
	{
		JButton button = new JButton(action);
		button.setText(name);
		if (icon != "") {
			button.setIcon(loadIcon(icon));
			button.setIconTextGap(5);
			button.setMargin(new Insets(button.getMargin().top, 3, button
				.getMargin().bottom, button.getMargin().right));
		}
		return button;
	}

	public static ImageIcon createIcon(final Color color) {
		final BufferedImage image = new BufferedImage(20, 10,
			BufferedImage.TYPE_INT_RGB);
		final Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.dispose();
		return new ImageIcon(image);
	}

	public static JPanel createCheckboxGroupedPanel(Holder<Boolean> visibility,
		String text, JComponent panel)
	{
		JPanel dark = new JPanel();
		dark.setLayout(new BorderLayout());
		JCheckBox checkbox = createCheckbox(visibility, text);
		JPanel title = new JPanel();
		title.setBackground(new Color(200, 200, 200));
		title.setLayout(new MigLayout("insets 4pt, gap 8pt, fillx", "10[][]10"));
		title.add(new JLabel(checkbox.getText()), "push");
		checkbox.setText("");
		checkbox.setOpaque(false);
		title.add(checkbox);
		dark.setBackground(new Color(200, 200, 200));
		dark.add(title, BorderLayout.PAGE_START);
		dark.add(panel, BorderLayout.CENTER);
		return dark;
	}

	private static JCheckBox createCheckbox(Holder<Boolean> visibility,
		String text)
	{
		final JCheckBox checkbox = new JCheckBox(text);
		checkbox.setSelected(visibility.get());
		visibility.notifier().add(checkbox::setSelected);
		checkbox.addItemListener(itemEvent -> visibility.set(itemEvent
			.getStateChange() == ItemEvent.SELECTED));
		return styleCheckboxUsingEye(checkbox);
	}

	public static JCheckBox styleCheckboxUsingEye(JCheckBox checkbox) {
		checkbox.setIcon(loadIcon("invisible.png"));
		checkbox.setSelectedIcon(loadIcon("visible.png"));
		checkbox.setPressedIcon(loadIcon("visible-hover.png"));
		checkbox.setRolloverIcon(loadIcon("invisible-hover.png"));
		checkbox.setRolloverSelectedIcon(loadIcon("visible-hover.png"));
		checkbox.setFocusable(false);
		return checkbox;
	}

	static MouseAdapter toMouseListener(DragBehaviour behavior) {
		return new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				behavior.init(e.getX(), e.getY());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				behavior.end(e.getX(), e.getY());
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				behavior.drag(e.getX(), e.getY());
			}
		};
	}

	public static JButton createIconButton(Action action) {
		JButton result = new JButton(action);
		result.setText("");
		result.setBorder(BorderFactory.createEmptyBorder());
		result.setContentAreaFilled(false);
		result.setOpaque(false);
		return result;
	}

	public static RunnableAction createAction(String title, Runnable action,
		String iconPath)
	{
		RunnableAction result = new RunnableAction(title, action);
		final ImageIcon icon = loadIcon(iconPath);
		result.putValue(Action.SMALL_ICON, icon);
		result.putValue(Action.LARGE_ICON_KEY, icon);
		return result;
	}

	public static ImageIcon loadIcon(String iconPath) {
		return new ImageIcon(GuiUtils.class.getResource("/images/" + iconPath));
	}
}
