package translation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Services (make translator final)
            final Translator translator = pickTranslator();          // JSONTranslator if available, else CanadaTranslator
            final CountryCodeConverter countryConv = new CountryCodeConverter();
            final LanguageCodeConverter langConv = new LanguageCodeConverter();

            // Data for widgets
            final List<String> countryCodes = translator.getCountryCodes();     // e.g., ["can","usa",...]
            final List<String> countryNames = new ArrayList<>(countryCodes.size());
            for (String code : countryCodes) {
                String name = countryConv.fromCountryCode(code);
                countryNames.add(name != null ? name : code.toUpperCase());     // fallback to code if missing
            }

            final List<String> languageCodes = translator.getLanguageCodes();   // e.g., ["en","de","es",...]
            final String[] languageCodeArray = languageCodes.toArray(new String[0]);

            // Widgets
            // Language dropdown
            final JComboBox<String> languageBox = new JComboBox<>(languageCodeArray);
            if (languageCodes.contains("en")) languageBox.setSelectedItem("en"); // default to English if present

            // Optional pretty names like "English (en)"; falls back to "en" if not found
            languageBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    String code = (String) value;
                    String display = code;
                    try {
                        String name = langConv.fromLanguageCode(code);
                        if (name != null && !name.isBlank()) display = name + " (" + code + ")";
                    } catch (Throwable ignored) { /* ignore, show code only */ }
                    return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
                }
            });

            // Countries list (scrollable)
            final JList<String> countryList = new JList<>(countryNames.toArray(new String[0]));
            countryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            countryList.setVisibleRowCount(10);
            final JScrollPane countryScroll = new JScrollPane(countryList);

            // Result field
            final JTextField resultField = new JTextField(24);
            resultField.setEditable(false);

            // Update logic
            final Runnable update = () -> {
                int idx = countryList.getSelectedIndex();
                if (idx < 0) return;

                String countryCode = countryCodes.get(idx);
                String langCode = (String) languageBox.getSelectedItem();
                if (langCode != null) langCode = langCode.trim().toLowerCase();

                String out = translator.translate(countryCode, langCode);
                resultField.setText(out != null ? out : "no translation found!");
            };

            languageBox.addActionListener(e -> update.run());
            countryList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) update.run(); });

            // Layout
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.add(new JLabel("Language:"));
            top.add(languageBox);

            JPanel center = new JPanel(new BorderLayout());
            center.add(new JLabel("Countries:"), BorderLayout.NORTH);
            center.add(countryScroll, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bottom.add(new JLabel("Translation:"));
            bottom.add(resultField);

            JPanel main = new JPanel(new BorderLayout(8, 8));
            main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            main.add(top, BorderLayout.NORTH);
            main.add(center, BorderLayout.CENTER);
            main.add(bottom, BorderLayout.SOUTH);

            JFrame frame = new JFrame("Country Name Translator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(main);
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);

            // default selections + initial render
            if (!countryNames.isEmpty()) countryList.setSelectedIndex(0);
            update.run();
        });
    }

    // Create translator once so it is final/effectively final for lambdas
    private static Translator pickTranslator() {
        try {
            return new JSONTranslator();       // uses sample.json in resources
        } catch (Throwable t) {
            return new CanadaTranslator();     // fallback so GUI still runs
        }
    }
}
