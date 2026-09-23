package vista;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class TemaNexcell {

    public static void aplicarTema() {
        FlatDarkLaf.setup();

        // Paleta de colores Nexcell
        Color naranjaNexcell = new Color(255, 120, 30);
        Color fondoPrincipal = new Color(10, 25, 47);
        Color celesteHover = new Color(60, 138, 255);
        Color naranjaClick = new Color(200, 90, 20);
        Color celesteClick = new Color(60, 138, 255);
        Color filaAlterna = new Color(18, 35, 60);
        Color grisBordes = new Color(35, 55, 80);

        UIManager.put("Component.accentColor", naranjaNexcell);
        UIManager.put("Panel.background", fondoPrincipal);
        UIManager.put("Button.arc", 15);
        UIManager.put("TextComponent.arc", 10);

        // --- CONFIGURACIÓN DE LOS BOTONES ---
        UIManager.put("Button.background", naranjaNexcell);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.hoverBackground", celesteHover);
        UIManager.put("Button.pressedBackground", naranjaClick);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));

        UIManager.put("Table.background", fondoPrincipal);
        UIManager.put("Table.alternateRowColor", filaAlterna);
        UIManager.put("Table.selectionBackground", naranjaNexcell);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", grisBordes);

        UIManager.put("TableHeader.background", filaAlterna);
        UIManager.put("TableHeader.foreground", naranjaNexcell);
        UIManager.put("TableHeader.bottomSeparatorColor", naranjaNexcell);

        UIManager.put("TabbedPane.tabHeight", 100);
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 15));

        UIManager.put("TabbedPane.selectedBackground", celesteClick);
        UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
        UIManager.put("TabbedPane.hoverColor", celesteHover);

        UIManager.put("TabbedPane.focusColor", new Color(0, 0, 0, 0));

        UIManager.put("TabbedPane.background", fondoPrincipal);
        UIManager.put("TabbedPane.foreground", Color.WHITE);
    }
}
