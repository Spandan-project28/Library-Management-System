package ui;

import library.LibraryManager;
import model.Book;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Reusable styled JTable that displays the book catalogue.
 */
public class BookTablePanel extends JPanel {

    private static final String[] COLUMNS =
            {"Book ID", "Title", "Author", "Genre", "Status", "Borrowed By", "Due Date"};

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final LibraryManager library;

    public BookTablePanel(LibraryManager library) {
        this.library = library;
        setBackground(UITheme.BG_PANEL);
        setLayout(new BorderLayout(0, 8));
        setBorder(UITheme.paddedBorder(10, 12));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(UITheme.BG_PANEL);
        scroll.getViewport().setBackground(UITheme.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));

        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    private void styleTable() {
        table.setBackground(UITheme.BG_DARK);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(UITheme.ACCENT_BLUE.darker());
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.BG_CARD);
        header.setForeground(UITheme.ACCENT_BLUE);
        header.setFont(UITheme.FONT_SUBTITLE);
        header.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {70, 220, 160, 130, 90, 90, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom row renderer for alternating rows and status colour
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    setBackground(row % 2 == 0 ? UITheme.BG_DARK : UITheme.TABLE_ALT_ROW);
                    String status = (String) tbl.getValueAt(row, 4);
                    if (col == 4) {
                        setForeground("AVAILABLE".equals(status)
                                ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
                    } else {
                        setForeground(UITheme.TEXT_PRIMARY);
                    }
                }
                return this;
            }
        });
    }

    /** Reloads all rows from the library. */
    public void refresh() {
        tableModel.setRowCount(0);
        List<Book> books = library.getAllBooks();
        for (Book b : books) {
            tableModel.addRow(b.toTableRow());
        }
    }

    /** Displays a filtered subset of books. */
    public void showBooks(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(b.toTableRow());
        }
    }

    public JTable getTable() { return table; }
}
