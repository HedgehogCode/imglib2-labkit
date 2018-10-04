
package net.imglib2.labkit.panel;

import net.imglib2.labkit.actions.SegmentationAsLabelAction;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;

public class SegmenterPanel {

	private final SegmenterListModel<? extends SegmentationItem> segmentationModel;

	private final JPanel panel = new JPanel();

	private final ComponentList<Object, JPanel> list = new ComponentList<>();

	private final SegmentationAsLabelAction sal;

	public SegmenterPanel(
		SegmenterListModel<? extends SegmentationItem> segmentationModel, SegmentationAsLabelAction sal)
	{
		this.segmentationModel = segmentationModel;
		this.sal = sal;
		panel.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[grow][]"));
		panel.add(initList(), "grow, wrap");
		panel.add(initBottomPanel(), "grow");
	}

	private JComponent initBottomPanel() {
		JPanel result = new JPanel();
		result.setBackground(UIManager.getColor("List.background"));
		result.setLayout(new MigLayout("insets 4pt, gap 4pt"));
		result.add(initAddButton());
		return result;
	}

	private JButton initAddButton() {
		return GuiUtils.createActionIconButton("Add classifier", new RunnableAction(
			"Add classifier", () -> {
				segmentationModel.addSegmenter();
				updateList();
			}), "add.png");
	}

	private void updateList() {
		list.clear();
		segmentationModel.segmenters().forEach(item -> list.add(item,
			new EntryPanel(item)));
		list.setSelected(segmentationModel.selectedSegmenter().get());
	}

	private class EntryPanel extends JPanel {

		private final SegmentationItem item;
		private final RunnableAction settingsAction = GuiUtils.createAction(
			"Settings ...", this::showSettings, "gear.png");
		private final RunnableAction trainAction = GuiUtils.createAction("Train",
			this::runTraining, "run.png");

		private EntryPanel(SegmentationItem item) {
			this.item = item;
			setLayout(new MigLayout("inset 4, gap 4, fillx"));
			add(new JLabel(item.toString()), "push");
			add(initPopupMenuButton());
			add(GuiUtils.createIconButton(settingsAction));
			add(GuiUtils.createIconButton(trainAction));
		}

		private JButton initPopupMenuButton() {
			JPopupMenu menu = initPopupMenu();
			JButton button = new BasicArrowButton(SwingConstants.SOUTH);
			button.addActionListener(actionEvent -> {
				menu.show(button, 0, button.getHeight());
			});
			return button;
		}

		private JPopupMenu initPopupMenu() {
			JPopupMenu menu = new JPopupMenu();
			menu.add(new JMenuItem(settingsAction));
			menu.add(new JMenuItem(trainAction));
			menu.add(removeMenuItem());
			menu.add(createLabelMenuItem());
			return menu;
		}

		private JMenuItem removeMenuItem() {
			final Runnable runnable = () -> {
				((SegmenterListModel) segmentationModel).remove(item);
				updateList();
			};
			return new JMenuItem(GuiUtils.createAction("Remove", runnable,
				"remove.png"));
		}

		private RunnableAction createLabelMenuItem() {
			return new RunnableAction("Create Label from Segmentation ...",
					() -> sal.addSegmentationAsLabel(item.results()));
		}

		private void showSettings() {
			item.segmenter().editSettings(null);
		}

		private void runTraining() {
			((SegmenterListModel) segmentationModel).selectedSegmenter().set(item);
			((SegmenterListModel) segmentationModel).train(item);
			updateList();
		}

	}

	private JComponent initList() {
		updateList();
		list.listeners().add(this::userChangedSelection);
		JComponent component = list.getComponent();
		component.setBorder(BorderFactory.createEmptyBorder());
		return component;
	}

	private void userChangedSelection() {
		Object selectedValue = list.getSelected();
		if (selectedValue != null) ((SegmenterListModel) segmentationModel)
			.selectedSegmenter().set(selectedValue);
	}

	public JComponent getComponent() {
		return panel;
	}
}
