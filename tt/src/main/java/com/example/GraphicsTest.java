package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Created by liangtg on 16-11-21.
 */

public class GraphicsTest extends JFrame implements ActionListener {
    public static final double PI = Math.PI / 180;
    JPanel panel;
    JPanel pnlCtl;
    JButton button;
    JButton button2;
    Graphics2D g2;
    Random random = new Random();

    public GraphicsTest(String string) {
        super(string);
    }

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        GraphicsTest testPanel = new GraphicsTest("分形树：QQ：三2824七676");
        testPanel.init();
    }

    public void init() {
        panel = new JPanel();
        pnlCtl = new JPanel();
        button = new JButton("分形树");
        button2 = new JButton("清除");
        this.add(panel, BorderLayout.CENTER);
        button.addActionListener(this);
        button2.addActionListener(this);
        pnlCtl.add(button);
        pnlCtl.add(button2);
        this.add(pnlCtl, BorderLayout.NORTH);
        setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        Dimension winSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((winSize.width - this.getWidth()) / 2, (winSize.height - this.getHeight()) / 2);
        g2 = (Graphics2D) panel.getGraphics();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("分形树".equals(e.getActionCommand())) {
//            drawLeaf(g2, 400, 500, 100, 210 + random.nextInt(100));
            drawLeg(g2, 400, 250, random.nextInt(700), random.nextInt(500), 0);
        } else if ("清除".equals(e.getActionCommand())) {
            panel.getGraphics().clearRect(0, 0, 800, 800);
        }
    }

    void drawLeg(Graphics g, double x1, double y1, double x2, double y2, int index) {
        if (index >= 2) return;
        g.setColor(new Color(0xFF0000FF, true));
        double length = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
        double cx, cy, coso, sino;
        coso = (x2 - x1) / length;
        sino = (y2 - y1) / length;
        double sind = Math.sin(Math.toRadians(15));
        double cosd = Math.cos(Math.toRadians(15));
        double sinn = sind * coso + sino * cosd;
        double cosn = cosd * coso - sind * sino;
        double lenn = length / 2 / cosd;
        double xn = lenn * cosn + x1;
        double yn = lenn * sinn + y1;
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        g.drawLine((int) x1, (int) y1, (int) xn, (int) yn);
        g.drawLine((int) xn, (int) yn, (int) x2, (int) y2);
        index++;
        drawLeg(g, x1, y1, xn, yn, index);
        drawLeg(g, xn, yn, x2, y2, index);
    }

    public void drawLeaf(Graphics g, double x, double y, double L, double a) {
        //random=new Random();
        //可以方面速度画以了解其算法
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int red = random.nextInt(127);
        int green = random.nextInt(127);
        int blue = random.nextInt(127);
//随机颜色
        g.setColor(new Color(red, green, blue));
        double x1, x2, x1L, x2L, x2R, x1R, y1, y2, y1L, y2L, y2R, y1R;
        float deflection = 50 - random.nextInt(20);//侧干主干的夹角
        float intersection = random.nextInt(40) - 20;//主干偏转角度
        float depth = 2 + random.nextInt(2);//限制递归深度
        float ratio = 3f;//主干侧干长度比(可调整使其更茂密或稀疏)
        float ratio2 = 1.2f;//上级主干与本级主干长度比（可调整使其变高低）
        if (L > depth && L > 10) {
            x2 = x + L * Math.cos(a * PI);
            y2 = y + L * Math.sin(a * PI);
            x2R = x2 + L / ratio * Math.cos((a + deflection) * PI);
            y2R = y2 + L / ratio * Math.sin((a + deflection) * PI);
            x2L = x2 + L / ratio * Math.cos((a - deflection) * PI);
            y2L = y2 + L / ratio * Math.sin((a - deflection) * PI);
            x1 = x + L / ratio * Math.cos(a * PI);
            y1 = y + L / ratio * Math.sin(a * PI);
            x1L = x1 + L / ratio * Math.cos((a - deflection) * PI);
            y1L = y1 + L / ratio * Math.sin((a - deflection) * PI);
            x1R = x1 + L / ratio * Math.cos((a + deflection) * PI);
            y1R = y1 + L / ratio * Math.sin((a + deflection) * PI);
            g.drawLine((int) x, (int) y, (int) x2, (int) y2);
            g.drawLine((int) x2, (int) y2, (int) x2R, (int) y2R);
            g.drawLine((int) x2, (int) y2, (int) x2L, (int) y2L);
            g.drawLine((int) x1, (int) y1, (int) x1L, (int) y1L);
            g.drawLine((int) x1, (int) y1, (int) x1R, (int) y1R);
            drawLeaf(g, x2, y2, L / ratio2, a + intersection);
            drawLeaf(g, x2R, y2R, L / ratio, a + deflection);
            drawLeaf(g, x2L, y2L, L / ratio, a - deflection);
            drawLeaf(g, x1L, y1L, L / ratio, a - deflection);
            drawLeaf(g, x1R, y1R, L / ratio, a + deflection);
        }
    }
}
