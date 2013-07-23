package tahrir.ui;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.SortedSet;

public class MicroblogDisplayPage {
	private final JComponent content;
	private final MicroblogTableModel tableModel;
    private final EventBus eventBus;
    private final Predicate<ParsedMicroblog> filter;

    public MicroblogDisplayPage(final Predicate<ParsedMicroblog> filter, final TrMainWindow mainWindow) {
        this.filter = filter;
        eventBus = mainWindow.node.eventBus;
		tableModel = new MicroblogTableModel();

		final JTable table = new JTable(tableModel);
		final MicroblogRenderer renderer = new MicroblogRenderer(mainWindow);
		// will allow it to fill entire scroll pane
		table.setFillsViewportHeight(true);
		// TODO: change the size as needed
		table.setRowHeight(110);
        table.setGridColor(new Color(244,242,242));
		table.setDefaultRenderer(ParsedMicroblog.class, renderer);
		table.setDefaultEditor(ParsedMicroblog.class, renderer);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(table);
		content = scrollPane;

        eventBus.register(this);

        final SortedSet<ParsedMicroblog> existingMicroblogs = mainWindow.node.mbClasses.mbsForViewing.getMicroblogSet();

        for (ParsedMicroblog parsedMicroblog : existingMicroblogs) {
            if (filter.apply(parsedMicroblog)) {
                tableModel.addNewMicroblog(parsedMicroblog);
            }
        }
    }

    @Subscribe
    public void modifyMicroblogsDisplay(MicroblogsModifiedEvent event){
        if(event.type.equals(MicroblogsModifiedEvent.ModificationType.RECIEVED)){
            if(filter.apply(event.parsedMb)){
                tableModel.addNewMicroblog(event.parsedMb);
            }
        }
    }

	public JComponent getContent() {
		return content;
	}

	@SuppressWarnings("serial")
	private class MicroblogTableModel extends AbstractTableModel {
		private final ArrayList<ParsedMicroblog> microblogs;
        // TODO: Use a separate Set so that we can efficiently check whether
        // microblogs are being added more than once

  		public MicroblogTableModel() {
			microblogs = Lists.newArrayList();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return microblogs.size();
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			return microblogs.get(row);
		}

		public void addNewMicroblog(final ParsedMicroblog mb) {
			microblogs.add(0, mb);
			// This is what updates the GUI with new microblogs.
			// Firing about the entire table here. It seems to be necessary.
			fireTableDataChanged();
		}

		public void removeMicroblog(final ParsedMicroblog mb) {
			final int mbIndex = microblogs.indexOf(mb);
			microblogs.remove(mbIndex);
			// should we fire a cell updated?
			//fireTableCellUpdated(mbIndex, 0);
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return ParsedMicroblog.class;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return null;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			// this allows clicking of buttons etc. in the table
			return true;
		}
	}
}
