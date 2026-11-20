package com.bank.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UITheme — Horizon Trust Bank Modern Design System (V2).
 * Styled with a sophisticated Deep Emerald / Midnight Slate & Teal palette,
 * anti-aliased rounded cards, and high-contrast accessible controls.
 */
public class UITheme {

    // ─── Color Palette (V2: Emerald, Midnight Slate & Teal) ──────────────────────
    public static final Color COLOR_PRIMARY         = new Color(15, 118, 110);   // Deep Emerald Teal #0F766E
    public static final Color COLOR_PRIMARY_HOVER   = new Color(13, 148, 136);   // Vibrant Teal #0D9488
    public static final Color COLOR_PRIMARY_PRESSED = new Color(17, 94, 89);     // Dark Pine #115E59

    public static final Color COLOR_SUCCESS         = new Color(16, 185, 129);   // Mint Emerald #10B981
    public static final Color COLOR_SUCCESS_HOVER   = new Color(5, 150, 105);    // #059669
    public static final Color COLOR_SUCCESS_PRESSED = new Color(4, 120, 87);

    public static final Color COLOR_DANGER          = new Color(225, 29, 72);    // Rose Crimson #E11D48
    public static final Color COLOR_DANGER_HOVER    = new Color(190, 18, 60);    // #BE123C
    public static final Color COLOR_DANGER_PRESSED  = new Color(159, 18, 57);

    public static final Color COLOR_PURPLE          = new Color(99, 102, 241);   // Electric Indigo #6366F1
    public static final Color COLOR_PURPLE_HOVER    = new Color(79, 70, 229);    // #4F46E5

    public static final Color COLOR_AMBER           = new Color(217, 119, 6);    // Warm Amber #D97706
    public static final Color COLOR_AMBER_HOVER     = new Color(180, 83, 9);     // #B45309

    public static final Color COLOR_BLUE            = new Color(2, 132, 199);    // Cobalt Blue #0284C7
    public static final Color COLOR_BLUE_HOVER      = new Color(3, 105, 161);    // #0369A1

    public static final Color COLOR_SECONDARY       = new Color(51, 65, 85);     // Slate #334155
    public static final Color COLOR_SECONDARY_HOVER = new Color(30, 41, 59);     // #1E293B

    public static final Color BG_MAIN               = new Color(248, 250, 252);  // Slate-50 #F8FAFC
    public static final Color BG_CARD               = Color.WHITE;
    public static final Color BG_DARK_HEADER        = new Color(15, 23, 42);     // Midnight Navy #0F172A
    public static final Color TEXT_DARK             = new Color(15, 23, 42);     // Charcoal #0F172A
    public static final Color TEXT_MUTED            = new Color(100, 116, 139);  // Slate-500 #64748B
    public static final Color BORDER_COLOR          = new Color(226, 232, 240);  // Slate-200 #E2E8F0
    public static final Color BORDER_FOCUS          = new Color(15, 118, 110);   // Emerald Focus #0F766E

    // ─── Typography ─────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);

    // ─── Modern Rounded Button ──────────────────────────────────────────────────
    public static class ModernButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        private Color pressedColor;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private int cornerRadius = 12;

        public ModernButton(String text, Color baseColor, Color hoverColor) {
            super(text);
            this.normalColor = baseColor;
            this.hoverColor = hoverColor;
            this.pressedColor = hoverColor.darker();

            setFont(FONT_BOLD);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color currentBg;
            if (!isEnabled()) {
                currentBg = new Color(203, 213, 225);
            } else if (isPressed) {
                currentBg = pressedColor;
            } else if (isHovered) {
                currentBg = hoverColor;
            } else {
                currentBg = normalColor;
            }

            // Fill rounded button
            g2.setColor(currentBg);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

            // Draw button text
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

            g2.setColor(isEnabled() ? getForeground() : new Color(148, 163, 184));
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }
    }

    // ─── Modern Text Field ──────────────────────────────────────────────────────
    public static class ModernTextField extends JTextField {
        private boolean isFocused = false;

        public ModernTextField(int columns) {
            super(columns);
            setFont(FONT_REGULAR);
            setForeground(TEXT_DARK);
            setBackground(Color.WHITE);
            setCaretColor(COLOR_PRIMARY);
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 8),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
            ));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(BORDER_FOCUS, 8),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)
                    ));
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(BORDER_COLOR, 8),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)
                    ));
                    repaint();
                }
            });
        }
    }

    // ─── Modern Password Field ──────────────────────────────────────────────────
    public static class ModernPasswordField extends JPasswordField {
        public ModernPasswordField(int columns) {
            super(columns);
            setFont(FONT_REGULAR);
            setForeground(TEXT_DARK);
            setBackground(Color.WHITE);
            setCaretColor(COLOR_PRIMARY);
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 8),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
            ));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(BORDER_FOCUS, 8),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)
                    ));
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(BORDER_COLOR, 8),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)
                    ));
                    repaint();
                }
            });
        }
    }

    // ─── Rounded Border Helper ──────────────────────────────────────────────────
    public static class RoundedBorder extends AbstractBorder {
        private Color color;
        private int radius;

        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(x + 1, y + 1, width - 2, height - 2, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }
    }

    // ─── Modern Card Panel with Subtle Accent Stripe ────────────────────────────
    public static class CardPanel extends JPanel {
        private int cornerRadius = 14;
        private Color topStripeColor = null;

        public CardPanel() {
            setOpaque(false);
        }

        public CardPanel(Color topStripeColor) {
            this.topStripeColor = topStripeColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fill card white background
            g2.setColor(BG_CARD);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            
            // Draw subtle top accent line if configured
            if (topStripeColor != null) {
                g2.setColor(topStripeColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), 4, cornerRadius, cornerRadius));
            }

            // Outer border
            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
