import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import javax.swing.text.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.swing.JFileChooser;

class Password implements Cloneable {
    private String website;
    private String username;
    private String password;
    private String securityCode;
    private List<String> passwordHistory;
    private boolean passwordVisibility;

    public Password(String website, String username, String password, String securityCode) {
        this.website = website;
        this.username = username;
        this.password = password;
        this.securityCode = securityCode;
        this.passwordVisibility = false;
        this.passwordHistory = new ArrayList<>();
    }


    public String getWebsite() {
        return website;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        if (passwordVisibility) {
            return password;
        } else {
            return "********";
        }
    }
    public List<String> getPasswordHistory() {
        return passwordHistory;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public boolean isPasswordVisible() {
        return passwordVisibility;
    }

    public void setPasswordVisibility(boolean visibility) {
        this.passwordVisibility = visibility;
    }

    public void setPassword(String password) {
        this.passwordHistory.add(this.password);  // Add old password to history
        this.password = password;
    }


    public String getPasswordStrengthLabel() {
        int length = password.length();

        if (length <= 5) {
            return "Weak";
        } else if (length <= 8) {
            return "Medium";
        } else {
            return "Good";
        }
    }



    @Override
    public Password clone() {
        try {
            return (Password) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

interface PasswordInput {
    String getWebsite();
    String getUsername();
    String getPassword();
}

class UsernamePasswordAdapter implements PasswordInput {
    private String website;
    private String username;
    private String password;

    public UsernamePasswordAdapter(String website, String username, String password) {
        this.website = website;
        this.username = username;
        this.password = password;
    }

    public String getWebsite() {
        return website;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

class PasswordMemento {
    private Password password;

    public PasswordMemento(Password password) {
        this.password = password.clone();
    }

    public Password getPassword() {
        return password;
    }
}

class PasswordCaretaker {
    private List<PasswordMemento> mementos;

    public PasswordCaretaker() {
        mementos = new ArrayList<>();
    }

    public void savePassword(Password password) {
        mementos.add(new PasswordMemento(password));
    }

    public void deletePassword(PasswordMemento passwordMemento) {
        mementos.remove(passwordMemento);
    }

    public List<PasswordMemento> getPasswords() {
        return new ArrayList<>(mementos);
    }
}

class PasswordManagerGUI extends JFrame {
    private JTextField websiteField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton saveButton;
    private JButton displayButton;
    private JPanel passwordDisplayPanel;
    private JScrollPane scrollPane;
    private JButton importButton;
    private PasswordCaretaker caretaker;
    private JTextField searchField;
    private JButton searchButton;

    private List<PasswordMemento> savedPasswords;

    public PasswordManagerGUI() {
        super("Password Manager");

        caretaker = new PasswordCaretaker();
        savedPasswords = new ArrayList<>();

        websiteField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String website = websiteField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (!website.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                    PasswordInput input = new UsernamePasswordAdapter(website, username, password);
                    Password newPassword = createPassword(input);
                    caretaker.savePassword(newPassword);
                    clearFields();
                    JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Security Code: " + newPassword.getSecurityCode());
                } else {
                    JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Please enter the website, username, and password.");
                }
            }
        });

        displayButton = new JButton("Display");
        displayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                savedPasswords = caretaker.getPasswords();
                updatePasswordDisplay();
            }
        });

        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String keyword = searchField.getText();
                searchPasswordsByUsername(keyword);
            }
        });

        importButton = new JButton("Import");
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Import File");
                int userSelection = fileChooser.showOpenDialog(PasswordManagerGUI.this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    String filename = file.getAbsolutePath();

                    importCredentialsFromFile(file);
                }
            }
        });


        passwordDisplayPanel = new JPanel(new GridLayout(0, 1));
        scrollPane = new JScrollPane(passwordDisplayPanel);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        mainPanel.add(new JLabel("Website:"), gbc);
        gbc.gridx++;
        mainPanel.add(websiteField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx++;
        mainPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx++;
        mainPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        mainPanel.add(saveButton, gbc);

        gbc.gridy++;
        mainPanel.add(displayButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Search by Username:"), gbc);
        gbc.gridx++;
        mainPanel.add(searchField, gbc);
        gbc.gridx++;
        mainPanel.add(searchButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Saved Passwords:"), gbc);

        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        mainPanel.add(scrollPane, gbc);

        JPanel importPanel = new JPanel();
        importPanel.add(importButton);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        JPanel parentPanel = new JPanel(new BorderLayout());
        parentPanel.add(contentPanel, BorderLayout.CENTER);
        parentPanel.add(importPanel, BorderLayout.SOUTH);

        setContentPane(parentPanel);
        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

    }

    private void searchPasswordsByUsername(String keyword) {
        List<PasswordMemento> matchingPasswords = new ArrayList<>();

        for (PasswordMemento passwordMemento : savedPasswords) {
            Password password = passwordMemento.getPassword();
            if (password.getUsername().contains(keyword)) {
                matchingPasswords.add(passwordMemento);
            }
        }

        savedPasswords = matchingPasswords;
        updatePasswordDisplay();
    }

    private Password createPassword(PasswordInput input) {
        String website = input.getWebsite();
        String username = input.getUsername();
        String password = input.getPassword();

        for (PasswordMemento passwordMemento : savedPasswords) {
            Password savedPassword = passwordMemento.getPassword();

            if (savedPassword.getWebsite().equalsIgnoreCase(website) && savedPassword.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(PasswordManagerGUI.this, "A password for the same website and username already exists.");
                return null;
            }

        }

        String securityCode = generateSecurityCode();
        return new Password(website, username, password, securityCode);
    }


    private String generateSecurityCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void clearFields() {
        websiteField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    private void displayPasswordHistory(Password password) {
        StringBuilder message = new StringBuilder("Password History for " + password.getWebsite() + ":\n");
        for (String oldPassword : password.getPasswordHistory()) {
            message.append("- ").append(oldPassword).append("\n");
        }
        JOptionPane.showMessageDialog(PasswordManagerGUI.this, message.toString());
    }

    private void importCredentialsFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String website = null;
            String username = null;
            String password = null;
            String securityCode = null;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Website: ")) {
                    website = line.substring("Website: ".length());
                } else if (line.startsWith("Username: ")) {
                    username = line.substring("Username: ".length());
                } else if (line.startsWith("Password: ")) {
                    password = line.substring("Password: ".length());
                    securityCode = generateSecurityCode();
                }

                if (website != null && username != null && password != null && securityCode != null) {
                    PasswordInput input = new UsernamePasswordAdapter(website, username, password);
                    Password newPassword = new Password(input.getWebsite(), input.getUsername(), input.getPassword(), securityCode);
                    caretaker.savePassword(newPassword);

                    displaySecurityCode(newPassword);

                    website = null;
                    username = null;
                    password = null;
                    securityCode = null;
                }
            }

            if (website != null && username != null && password != null && securityCode != null) {
                PasswordInput input = new UsernamePasswordAdapter(website, username, password);
                Password newPassword = new Password(input.getWebsite(), input.getUsername(), input.getPassword(), securityCode);
                caretaker.savePassword(newPassword);

                displaySecurityCode(newPassword);
            }

            savedPasswords = caretaker.getPasswords();
            updatePasswordDisplay();

            JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Credentials imported from: " + file.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Error importing credentials.");
        }
    }

    private void displaySecurityCode(Password password) {
        String message = "Website: " + password.getWebsite() +
                "\nUsername: " + password.getUsername() +
                "\nPassword: " + password.getPassword() +
                "\nSecurity Code: " + password.getSecurityCode();

        JOptionPane.showMessageDialog(PasswordManagerGUI.this, message);
    }



    private void updatePasswordDisplay() {
        passwordDisplayPanel.removeAll();

        for (PasswordMemento passwordMemento : savedPasswords) {
            Password password = passwordMemento.getPassword();

            JPanel entryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JLabel websiteLabel = new JLabel("Website: " + password.getWebsite());
            JLabel usernameLabel = new JLabel("Username: " + password.getUsername());
            JLabel passwordLabel = new JLabel("Password: " + password.getPassword());
            JLabel strengthLabel = new JLabel("Strength: " + password.getPasswordStrengthLabel());


            JButton viewButton = new JButton("View");
            viewButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String enteredCode = JOptionPane.showInputDialog(PasswordManagerGUI.this, "Enter security code:");
                    if (enteredCode != null && !enteredCode.isEmpty()) {
                        if (enteredCode.equals(password.getSecurityCode())) {
                            password.setPasswordVisibility(true);
                            updatePasswordDisplay();
                        } else {
                            JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Invalid security code.");
                        }
                    }
                }
            });

            JButton updateButton = new JButton("Update");
            updateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String enteredCode = JOptionPane.showInputDialog(PasswordManagerGUI.this, "Enter security code:");
                    if (enteredCode != null && !enteredCode.isEmpty()) {
                        if (enteredCode.equals(password.getSecurityCode())) {
                            String newPassword = JOptionPane.showInputDialog(PasswordManagerGUI.this, "Enter new password:");
                            if (newPassword != null) {
                                password.setPassword(newPassword);
                                updatePasswordDisplay();
                            }
                        } else {
                            JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Invalid security code.");
                        }
                    }
                }
            });

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String enteredCode = JOptionPane.showInputDialog(PasswordManagerGUI.this, "Enter security code:");
                    if (enteredCode != null && !enteredCode.isEmpty()) {
                        if (enteredCode.equals(password.getSecurityCode())) {
                            int confirm = JOptionPane.showConfirmDialog(
                                    PasswordManagerGUI.this,
                                    "Are you sure you want to delete this password?",
                                    "Confirm Deletion",
                                    JOptionPane.YES_NO_OPTION
                            );

                            if (confirm == JOptionPane.YES_OPTION) {
                                caretaker.deletePassword(passwordMemento);
                                savedPasswords = caretaker.getPasswords();
                                updatePasswordDisplay();
                            }
                        } else {
                            JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Invalid security code.");
                        }
                    }
                }
            });


            JButton exportButton = new JButton("Export");
            exportButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String enteredCode = JOptionPane.showInputDialog(PasswordManagerGUI.this, "Enter security code:");
                    if (enteredCode != null && !enteredCode.isEmpty()) {
                        if (enteredCode.equals(password.getSecurityCode())) {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setDialogTitle("Select Export Location");
                            int userSelection = fileChooser.showSaveDialog(PasswordManagerGUI.this);

                            if (userSelection == JFileChooser.APPROVE_OPTION) {
                                File file = fileChooser.getSelectedFile();
                                String filename = file.getAbsolutePath();

                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                                    writer.write("Website: " + password.getWebsite());
                                    writer.newLine();
                                    writer.write("Username: " + password.getUsername());
                                    writer.newLine();
                                    writer.write("Password: " + password.getPassword());
                                    writer.newLine();
                                    writer.flush();

                                    JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Credentials exported to: " + filename);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Error exporting credentials.");
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Invalid security code.");
                        }
                    }
                }
            });

            JButton historyButton = new JButton("History");
            historyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int selectedIndex = passwordDisplayPanel.getComponentZOrder(((JButton) e.getSource()).getParent());
                    PasswordMemento passwordMemento = savedPasswords.get(selectedIndex);
                    Password password = passwordMemento.getPassword();
                    displayPasswordHistory(password);
                }
            });


            entryPanel.add(websiteLabel);
            entryPanel.add(usernameLabel);
            entryPanel.add(passwordLabel);
            entryPanel.add(strengthLabel);
            entryPanel.add(viewButton);
            entryPanel.add(updateButton);
            entryPanel.add(deleteButton);
            entryPanel.add(exportButton);
            entryPanel.add(historyButton);

            passwordDisplayPanel.add(entryPanel);
        }

        passwordDisplayPanel.revalidate();
        passwordDisplayPanel.repaint();
    }
}


public class PasswordManagerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PasswordManagerGUI();
            }
        });
    }
}