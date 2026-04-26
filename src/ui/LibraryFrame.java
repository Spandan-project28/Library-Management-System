package ui;

import exceptions.BookNotAvailableException;
import exceptions.InvalidStudentIdException;
import exceptions.LimitExceededException;
import library.LibraryManager;
import model.Book;
import threads.ConcurrentBorrowSimulator;
import threads.OverdueCheckerThread;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window — Smart Library Issue and Return System.
 *
 * Swing components used:
 *   JFrame, JLabel, JTextField, JButton, JTable (via BookTablePanel),
 *   JComboBox, JTextArea, JPanel, JTabbedPane, JScrollPane, JOptionPane
 */
public class LibraryFrame extends JFrame {

    private final LibraryManager library;

    // ---- Shared panels ----
    private BookTablePanel bookTablePanel;
    private JTextArea      activityArea;
    private JLabel         overdueLabel;
    private JLabel         statusBar;

    // ---- Add-book fields ----
    private JTextField addIdField, addTitleField, addAuthorField;
    private JComboBox<String> addGenreCombo;

    // ---- Search field ----
    private JTextField searchField;

    // ---- Borrow / Return fields ----
    private JTextField borrowStudentField, borrowBookField;
    private JTextField returnStudentField, returnBookField;

    // ---- Register Student fields ----
    private JTextField regIdField, regNameField;

    // ---- Simulation genre combo ----
    private JComboBox<String> simStudentCombo;

    // ---- Threads ----
    private OverdueCheckerThread overdueThread;

    public LibraryFrame(LibraryManager library) {
        this.library = library;
        UITheme.applyGlobalDefaults();
        initFrame();
        buildUI();
        startBackgroundThreads();
        refreshAll();
    }

    // =========================================================
    //  Frame setup
    // =========================================================
    private void initFrame() {
        setTitle("📚 Smart Library System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);
    }

    // =========================================================
    //  Build entire UI
    // =========================================================
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ---- Header ----
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(UITheme.BG_CARD);
        hdr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.ACCENT_BLUE),
                UITheme.paddedBorder(12, 20)));

        JLabel title = UITheme.label("📚  Smart Library Issue & Return System",
                UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        hdr.add(title, BorderLayout.WEST);

        overdueLabel = UITheme.label("⏰  Overdue: 0 book(s)", UITheme.FONT_SUBTITLE, UITheme.ACCENT_AMBER);
        hdr.add(overdueLabel, BorderLayout.EAST);
        return hdr;
    }

    // ---- Status bar ----
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        bar.setBackground(UITheme.BG_CARD);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        statusBar = UITheme.label("Ready.", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        bar.add(statusBar);
        return bar;
    }

    // ---- Centre — tabbed pane ----
    private JComponent buildCenter() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_DARK);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.setFont(UITheme.FONT_SUBTITLE);
        tabs.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        tabs.addTab("📖  Catalogue",     buildCatalogueTab());
        tabs.addTab("➕  Add Book",       buildAddBookTab());
        tabs.addTab("🎓  Register Student", buildRegisterStudentTab());
        tabs.addTab("📤  Borrow",         buildBorrowTab());
        tabs.addTab("📥  Return",         buildReturnTab());
        tabs.addTab("📋  Activity Log",   buildActivityTab());
        tabs.addTab("⚡  Simulate",       buildSimulateTab());

        return tabs;
    }

    // =========================================================
    //  TAB 1 — Catalogue
    // =========================================================
    private JPanel buildCatalogueTab() {
        JPanel panel = darkPanel(new BorderLayout(0, 8));
        panel.setBorder(UITheme.paddedBorder(10, 10));

        // Search bar
        JPanel searchBar = darkPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.add(UITheme.label("🔍 Search:", UITheme.FONT_SUBTITLE, UITheme.TEXT_SECONDARY));
        searchField = UITheme.textField(30);
        searchField.setToolTipText("Search by ID, title, author, or genre");
        searchBar.add(searchField);

        JButton searchBtn = UITheme.button("Search", UITheme.ACCENT_BLUE);
        searchBtn.setName("btn_search");
        searchBtn.addActionListener(e -> doSearch());
        searchBar.add(searchBtn);

        JButton showAllBtn = UITheme.button("Show All", UITheme.BG_CARD);
        showAllBtn.addActionListener(e -> { searchField.setText(""); bookTablePanel.refresh(); setStatus("Showing all books."); });
        searchBar.add(showAllBtn);

        panel.add(searchBar, BorderLayout.NORTH);

        bookTablePanel = new BookTablePanel(library);
        panel.add(bookTablePanel, BorderLayout.CENTER);

        JButton refreshBtn = UITheme.button("⟳ Refresh", UITheme.ACCENT_PURPLE);
        refreshBtn.addActionListener(e -> bookTablePanel.refresh());
        JPanel bottom = darkPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    //  TAB 2 — Add Book
    // =========================================================
    private JPanel buildAddBookTab() {
        JPanel outer = darkPanel(new GridBagLayout());
        JPanel card  = cardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(480, 360));

        GridBagConstraints gbc = formGbc();

        card.add(UITheme.label("Add New Book", UITheme.FONT_TITLE, UITheme.ACCENT_BLUE), titleGbc());

        addIdField     = addFormRow(card, gbc, 0, "Book ID",  "e.g. B011");
        addTitleField  = addFormRow(card, gbc, 1, "Title",    "e.g. Head First Java");
        addAuthorField = addFormRow(card, gbc, 2, "Author",   "e.g. Kathy Sierra");
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        card.add(UITheme.label("Genre:", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY), gbc);
        addGenreCombo = new JComboBox<>(new String[]{"Programming", "Computer Science", "Software Engineering", "Java", "Systems", "Networking", "Databases", "AI", "Fiction", "Other"});
        UITheme.styleCombo(addGenreCombo);
        gbc.gridx = 1;
        card.add(addGenreCombo, gbc);

        JButton addBtn = UITheme.button("➕  Add Book", UITheme.ACCENT_GREEN);
        addBtn.setName("btn_add_book");
        addBtn.addActionListener(e -> doAddBook());
        gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 8, 8, 8);
        card.add(addBtn, gbc);

        outer.add(card);
        return outer;
    }

    // =========================================================
    //  TAB 3 — Register Student
    // =========================================================
    private JPanel buildRegisterStudentTab() {
        JPanel outer = darkPanel(new GridBagLayout());
        JPanel card  = cardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 280));

        GridBagConstraints gbc = formGbc();
        card.add(UITheme.label("Register Student", UITheme.FONT_TITLE, UITheme.ACCENT_PURPLE), titleGbc());

        regIdField   = addFormRow(card, gbc, 0, "Student ID", "e.g. S201 (alphanumeric)");
        regNameField = addFormRow(card, gbc, 1, "Full Name",  "e.g. Jane Doe");

        JButton regBtn = UITheme.button("🎓  Register", UITheme.ACCENT_PURPLE);
        regBtn.setName("btn_register_student");
        regBtn.addActionListener(e -> doRegisterStudent());
        gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 8, 8, 8);
        card.add(regBtn, gbc);

        outer.add(card);
        return outer;
    }

    // =========================================================
    //  TAB 4 — Borrow
    // =========================================================
    private JPanel buildBorrowTab() {
        JPanel outer = darkPanel(new GridBagLayout());
        JPanel card  = cardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(440, 280));

        GridBagConstraints gbc = formGbc();
        card.add(UITheme.label("Borrow a Book", UITheme.FONT_TITLE, UITheme.ACCENT_GREEN), titleGbc());

        borrowStudentField = addFormRow(card, gbc, 0, "Student ID", "Registered student ID");
        borrowBookField    = addFormRow(card, gbc, 1, "Book ID",    "Available book ID");

        JButton borrowBtn = UITheme.button("📤  Borrow", UITheme.ACCENT_GREEN);
        borrowBtn.setName("btn_borrow");
        borrowBtn.addActionListener(e -> doBorrow());
        gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 8, 8, 8);
        card.add(borrowBtn, gbc);

        outer.add(card);
        return outer;
    }

    // =========================================================
    //  TAB 5 — Return
    // =========================================================
    private JPanel buildReturnTab() {
        JPanel outer = darkPanel(new GridBagLayout());
        JPanel card  = cardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(440, 280));

        GridBagConstraints gbc = formGbc();
        card.add(UITheme.label("Return a Book", UITheme.FONT_TITLE, UITheme.ACCENT_RED), titleGbc());

        returnStudentField = addFormRow(card, gbc, 0, "Student ID", "Your registered student ID");
        returnBookField    = addFormRow(card, gbc, 1, "Book ID",    "Book ID to return");

        JButton returnBtn = UITheme.button("📥  Return", UITheme.ACCENT_RED);
        returnBtn.setName("btn_return");
        returnBtn.addActionListener(e -> doReturn());
        gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 8, 8, 8);
        card.add(returnBtn, gbc);

        outer.add(card);
        return outer;
    }

    // =========================================================
    //  TAB 6 — Activity Log
    // =========================================================
    private JPanel buildActivityTab() {
        JPanel panel = darkPanel(new BorderLayout(0, 8));
        panel.setBorder(UITheme.paddedBorder(10, 12));

        panel.add(UITheme.label("Recent Activity Log  (newest first)",
                UITheme.FONT_SUBTITLE, UITheme.ACCENT_BLUE), BorderLayout.NORTH);

        activityArea = UITheme.textArea();
        JScrollPane scroll = new JScrollPane(activityArea);
        scroll.getViewport().setBackground(UITheme.INPUT_BG);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        panel.add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = UITheme.button("⟳ Refresh Log", UITheme.ACCENT_PURPLE);
        refreshBtn.addActionListener(e -> refreshActivityLog());
        JPanel bot = darkPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    //  TAB 7 — Simulate Concurrent Borrowing
    // =========================================================
    private JPanel buildSimulateTab() {
        JPanel panel = darkPanel(new BorderLayout(0, 12));
        panel.setBorder(UITheme.paddedBorder(16, 20));

        // Info card
        JPanel infoCard = cardPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.add(UITheme.label("⚡ Concurrent Borrow Simulation",
                UITheme.FONT_TITLE, UITheme.ACCENT_AMBER));
        infoCard.add(Box.createVerticalStrut(8));
        infoCard.add(UITheme.label(
                "Launches multiple student threads simultaneously to borrow books.",
                UITheme.FONT_BODY, UITheme.TEXT_SECONDARY));
        infoCard.add(UITheme.label(
                "Demonstrates synchronized access, try/catch/finally, and thread coordination.",
                UITheme.FONT_BODY, UITheme.TEXT_SECONDARY));
        panel.add(infoCard, BorderLayout.NORTH);

        // Output area
        JTextArea simOutput = UITheme.textArea();
        simOutput.setName("sim_output");
        JScrollPane scroll = new JScrollPane(simOutput);
        scroll.getViewport().setBackground(UITheme.INPUT_BG);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        panel.add(scroll, BorderLayout.CENTER);

        // Button
        JButton simBtn = UITheme.button("▶  Run Simulation", UITheme.ACCENT_AMBER);
        simBtn.setName("btn_simulate");
        simBtn.addActionListener(e -> {
            simOutput.setText("");
            simBtn.setEnabled(false);
            List<String> sIds = new ArrayList<>(library.getAllStudentIds());
            List<String> bIds = new ArrayList<>();
            library.getAllBooks().forEach(b -> bIds.add(b.getBookId()));

            if (sIds.isEmpty() || bIds.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No students or books to simulate with.", "Simulation Error",
                        JOptionPane.WARNING_MESSAGE);
                simBtn.setEnabled(true);
                return;
            }

            ConcurrentBorrowSimulator sim = new ConcurrentBorrowSimulator(
                    library,
                    new ConcurrentBorrowSimulator.SimulationListener() {
                        public void onSimulationEvent(String msg, boolean err) {
                            SwingUtilities.invokeLater(() -> {
                                simOutput.append(msg + "\n");
                                refreshAll();
                            });
                        }
                        public void onSimulationComplete() {
                            SwingUtilities.invokeLater(() -> {
                                simOutput.append("\n── Simulation complete ──\n");
                                simBtn.setEnabled(true);
                                refreshAll();
                            });
                        }
                    },
                    sIds, bIds);
            sim.startSimulation();
        });

        JPanel bot = darkPanel(new FlowLayout(FlowLayout.CENTER));
        bot.add(simBtn);
        panel.add(bot, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    //  Actions
    // =========================================================

    private void doSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { bookTablePanel.refresh(); return; }
        List<Book> results = library.searchBooks(q);
        bookTablePanel.showBooks(results);
        setStatus("Found " + results.size() + " book(s) matching '" + q + "'.");
    }

    private void doAddBook() {
        String id     = addIdField.getText().trim();
        String title  = addTitleField.getText().trim();
        String author = addAuthorField.getText().trim();
        String genre  = (String) addGenreCombo.getSelectedItem();

        try {
            if (id.isEmpty() || title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                throw new IllegalArgumentException("All fields are required.");
            }
            library.addBook(id, title, author, genre);
            clearFields(addIdField, addTitleField, addAuthorField);
            addGenreCombo.setSelectedIndex(0);
            refreshAll();
            setStatus("Book [" + id + "] added successfully.");
            JOptionPane.showMessageDialog(this,
                    "Book '" + title + "' added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError("Add Book Error", ex.getMessage());
        } finally {
            addIdField.requestFocus();
        }
    }

    private void doRegisterStudent() {
        String id   = regIdField.getText().trim();
        String name = regNameField.getText().trim();
        try {
            if (name.isEmpty()) throw new IllegalArgumentException("Student name cannot be empty.");
            library.registerStudent(id, name);
            clearFields(regIdField, regNameField);
            refreshAll();
            setStatus("Student [" + id + "] registered.");
            JOptionPane.showMessageDialog(this,
                    "Student '" + name + "' registered!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (exceptions.InvalidStudentIdException ex) {
            showError("Invalid Student ID", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError("Registration Error", ex.getMessage());
        } finally {
            regIdField.requestFocus();
        }
    }

    private void doBorrow() {
        String sid = borrowStudentField.getText().trim();
        String bid = borrowBookField.getText().trim();
        try {
            library.borrowBook(sid, bid);
            clearFields(borrowStudentField, borrowBookField);
            refreshAll();
            setStatus("Book [" + bid + "] borrowed by student [" + sid + "].");
            JOptionPane.showMessageDialog(this,
                    "Book [" + bid + "] borrowed successfully!\nDue in 14 days.", "Borrowed",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (InvalidStudentIdException ex) {
            showError("Invalid Student ID", ex.getMessage());
        } catch (BookNotAvailableException ex) {
            showError("Book Not Available", ex.getMessage());
        } catch (LimitExceededException ex) {
            showError("Borrow Limit Exceeded", ex.getMessage());
        } finally {
            borrowStudentField.requestFocus();
        }
    }

    private void doReturn() {
        String sid = returnStudentField.getText().trim();
        String bid = returnBookField.getText().trim();
        try {
            library.returnBook(sid, bid);
            clearFields(returnStudentField, returnBookField);
            refreshAll();
            setStatus("Book [" + bid + "] returned by student [" + sid + "].");
            JOptionPane.showMessageDialog(this,
                    "Book [" + bid + "] returned successfully!", "Returned",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (InvalidStudentIdException ex) {
            showError("Invalid Student ID", ex.getMessage());
        } catch (BookNotAvailableException ex) {
            showError("Return Error", ex.getMessage());
        } finally {
            returnStudentField.requestFocus();
        }
    }

    // =========================================================
    //  Background threads
    // =========================================================
    private void startBackgroundThreads() {
        overdueThread = new OverdueCheckerThread(library, (overdueBooks, count) ->
                SwingUtilities.invokeLater(() -> {
                    overdueLabel.setText("⏰  Overdue: " + count + " book(s)");
                    overdueLabel.setForeground(count > 0 ? UITheme.ACCENT_RED : UITheme.ACCENT_AMBER);
                }),
                30_000L   // check every 30 seconds
        );
        overdueThread.start();
    }

    // =========================================================
    //  Refresh helpers
    // =========================================================
    private void refreshAll() {
        bookTablePanel.refresh();
        refreshActivityLog();
    }

    private void refreshActivityLog() {
        List<String> log = library.getActivityLog();
        StringBuilder sb = new StringBuilder();
        for (String line : log) sb.append(line).append("\n");
        activityArea.setText(sb.toString());
        activityArea.setCaretPosition(0);
    }

    private void setStatus(String msg) {
        statusBar.setText(msg);
    }

    // =========================================================
    //  Utility / layout helpers
    // =========================================================
    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(UITheme.BG_DARK);
        return p;
    }

    private JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                UITheme.cardBorder(),
                UITheme.paddedBorder(20, 28)));
        return p;
    }

    private GridBagConstraints formGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(8, 8, 8, 8);
        g.anchor  = GridBagConstraints.WEST;
        return g;
    }

    private GridBagConstraints titleGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(0, 0, 20, 0);
        return g;
    }

    /** Adds a label + text field row and returns the field. */
    private JTextField addFormRow(JPanel card, GridBagConstraints gbc,
                                  int row, String labelText, String tooltip) {
        gbc.gridx = 0; gbc.gridy = row + 1; gbc.gridwidth = 1;
        card.add(UITheme.label(labelText + ":", UITheme.FONT_BODY, UITheme.TEXT_SECONDARY), gbc);

        JTextField tf = UITheme.textField(22);
        tf.setToolTipText(tooltip);
        gbc.gridx = 1;
        card.add(tf, gbc);
        return tf;
    }

    private void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
