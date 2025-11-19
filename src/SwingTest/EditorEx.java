package SwingTest;
import javax.swing.*;
import java.awt.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.*;

import java.util.*;
import SwingTest.MyPoint;

import java.awt.Color;

public class EditorEx extends JFrame {
	
	private Color currentColor = null; 
	private int selectedOption = 0;
	private DrawingPanel drawPane = null;
	
	
    public EditorEx() {
        setTitle("Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
		JMenuBar menubar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open");
		JMenuItem saveItem = new JMenuItem("Save");
		JMenuItem closeItem = new JMenuItem("Close");
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(closeItem);
		
		menubar.add(fileMenu);
		
		
		JMenu editMenu = new JMenu("Edit");
		JMenuItem copyItem = new JMenuItem("Copy");
		JMenuItem cutItem = new JMenuItem("Cut");
		JMenuItem pasteItem = new JMenuItem("Paste");
		editMenu.add(copyItem);
		editMenu.add(cutItem);
		editMenu.add(pasteItem);
		
		menubar.add(editMenu);
		
		JMenu penOption = new JMenu("Pen");
		JMenuItem spline = new JMenuItem("Spline");
		JMenuItem colorSelect = new JMenuItem("Pen Color");
		
		JMenu eraserOption = new JMenu("Eraser");
		JMenuItem eraser = new JMenuItem("Eraser");
		JMenuItem erase_all = new JMenuItem("Erase All");

		penOption.add(spline);
		penOption.add(colorSelect);
		
		eraserOption.add(eraser);
		eraserOption.add(erase_all);
		
		menubar.add(penOption);
		menubar.add(eraserOption);
		
		setJMenuBar(menubar);
		
		
		//spline 버튼 클릭 시
		spline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	selectedOption = 1;
            	
            }
        });

		//eraser 버튼 클릭 시
		eraser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	selectedOption = 2;
            	
            }
        });
		
		erase_all.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (drawPane.splineList != null) drawPane.splineList.clear();
            	if (drawPane.currentSpline != null) drawPane.currentSpline.clear();
            	if (drawPane.currentErase != null) drawPane.currentErase.clear();
            	if (drawPane.writtenLog != null) drawPane.writtenLog.clear();
            	if (drawPane.eraserList != null) drawPane.eraserList.clear();
            	drawPane.repaint();
            }
        });
		
		//colorSelect 버튼 클릭 시
		colorSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // JColorChooser를 사용하여 색상 선택
            	currentColor = JColorChooser.showDialog(EditorEx.this, "Select Color", Color.BLACK);

            }
        });
        
        
        
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        //BorderLayout.East, West, South, North, Center

        drawPane = new DrawingPanel();
        drawPane.setBackground(Color.WHITE);
        c.add(drawPane, BorderLayout.CENTER);

        
        JTextPane consolePane = new JTextPane();
        consolePane.setText("Compiling......");
        JScrollPane consoleScrollPane = new JScrollPane(consolePane);
        consoleScrollPane.setPreferredSize(new Dimension(100, 100));
        c.add(consoleScrollPane, BorderLayout.SOUTH);
        
               
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("hello.java");
        model.addElement("world.java");
        
		JList<String> list = new JList<>(model);
		
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setPreferredSize(new Dimension(150, 100));
		
		c.add(listScrollPane, BorderLayout.EAST);
				
		  // 루트 노드 생성
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("SRC");

        // 자식 노드 추가
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("GUI");
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("lib");

        root.add(child1);
        root.add(child2);

        // 자식의 자식 노드 추가
        DefaultMutableTreeNode grandchild1 = new DefaultMutableTreeNode("hello.java");
        DefaultMutableTreeNode grandchild2 = new DefaultMutableTreeNode("world.java");

        child1.add(grandchild1);
        child2.add(grandchild2);

        // JTree 생성
        JTree tree = new JTree(root);

        // JScrollPane에 JTree 추가
        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setPreferredSize(new Dimension(150, 100));
        
        c.add(treeScrollPane, BorderLayout.WEST);

		pack(); 
		setSize(1024, 712);
        setVisible(true);
    }

    
    //개중요
    class DrawingPanel extends JPanel{
    	
    	private Vector<Vector<MyPoint>> splineList;
    	private Vector<Vector<MyPoint>> eraserList;
    	private Vector<MyPoint> currentSpline = null;
    	private Vector<MyPoint> currentErase = null;
    	private Vector<Object> writtenLog;

    	MyPoint start = null; 
    	MyPoint end = null; 
    	
    	DrawingPanel(){
    		splineList = new Vector<Vector<MyPoint>>();
    		eraserList = new Vector<Vector<MyPoint>>();
    		start = new MyPoint();
    		end = new MyPoint();
    		writtenLog = new Vector<Object>();
    		
    		
    	   	addMouseListener(new MouseAdapter(){
        		public void mousePressed(MouseEvent e) {
        			start.x = e.getX();
        			start.y = e.getY();
    				System.out.println(start.x + ", " + start.y);
    				
    				if (EditorEx.this.selectedOption == 1) {
    					currentSpline = new Vector<>();
        				MyPoint p = new MyPoint(e.getX(), e.getY());
        				currentSpline.add(p);
        				splineList.add(currentSpline);
            	   		p.setColor(EditorEx.this.currentColor);
    				} else if (EditorEx.this.selectedOption == 2) {
    					currentErase = new Vector<>();
        				MyPoint p = new MyPoint(e.getX(), e.getY());
        				currentErase.add(p);
        				eraserList.add(currentErase);
            	   		p.setColor(new Color(255, 255, 255));
    				}
        		}
        		
    	   	});	
    	   	
    	   	addMouseMotionListener(new MouseMotionAdapter() {
    	   		public void mouseDragged(MouseEvent e) {
    	   			if (EditorEx.this.selectedOption == 1) {
//        				System.out.println(e.getX() + ", " + e.getY());
        				MyPoint p = new MyPoint(e.getX(), e.getY());
            	   		p.setColor(EditorEx.this.currentColor);
            	   		System.out.println(EditorEx.this.currentColor);
        				currentSpline.add(p);
        				writtenLog.add(p);
        				repaint();
                    } else if (EditorEx.this.selectedOption == 2) {
//        				System.out.println(e.getX() + ", " + e.getY());
        				MyPoint p = new MyPoint(e.getX(), e.getY());
            	   		p.setColor(new Color(255, 255, 255));
            	   		currentErase.add(p);
            	   		writtenLog.add(p);
        				repaint();
                    }
    	   		}
    	   	});
    	}
    	
    	public void paintComponent(Graphics g) {
    		super.paintComponent(g);
			
			for(Vector<MyPoint> spline : splineList) {
				Graphics2D g2 = (Graphics2D) g;
	    		g2.setStroke(new BasicStroke(3));
				for (int i = 1; i < spline.size(); i++) {
					MyPoint temp1 = spline.get(i-1);
					MyPoint temp2 = spline.get(i);
					g.setColor(temp1.pointColor);
					g.drawLine(temp1.x, temp1.y, temp2.x, temp2.y);
				}
			}
			
			for(Vector<MyPoint> eraser : eraserList) {
				Graphics2D g2 = (Graphics2D) g;
	    		g2.setStroke(new BasicStroke(10));
				for (int i = 1; i < eraser.size(); i++) {
					MyPoint temp1 = eraser.get(i-1);
					MyPoint temp2 = eraser.get(i);
					g.setColor(temp1.pointColor);
					g.drawLine(temp1.x, temp1.y, temp2.x, temp2.y);
				}
			}

			
    	}

    }
    
    public static void main(String[] args) {
        new EditorEx();
    }
}