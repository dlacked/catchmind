package SwingTest;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.*;
import SwingTest.MyPoint;

import java.awt.Color;

public class EditorEx extends JFrame {
	
	private Color currentColor = Color.BLACK; 
	private DrawingPanel drawPane = null;
	private int penWidth = 3;
	
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
		
		JMenu penOption = new JMenu("Pen Option");
		JMenuItem colorSelect = new JMenuItem("Pen Color");
		JMenuItem widthInput = new JMenuItem("Pen Width");
		
		JMenu eraserOption = new JMenu("Eraser");
		JMenuItem undo = new JMenuItem("Undo");
		JMenuItem redo = new JMenuItem("Redo");
		JMenuItem erase_all = new JMenuItem("Erase All");

		penOption.add(colorSelect);
		penOption.add(widthInput);
		
		eraserOption.add(undo);
		eraserOption.add(redo);
		eraserOption.add(erase_all);
		
		menubar.add(penOption);
		menubar.add(eraserOption);
		
		setJMenuBar(menubar);

		//eraser 버튼 클릭 시
		undo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (drawPane.splineList.size() != 0) {
            		drawPane.erasedSplineList.add(drawPane.splineList.get(drawPane.splineList.size()-1));
            		drawPane.splineList.remove(drawPane.splineList.size()-1);
                	drawPane.repaint();
            	}
            	System.out.println("splineList:" + drawPane.splineList.size());
            	System.out.println("erasedSplineList:" + drawPane.erasedSplineList.size());
            	
            }
        });
		
		redo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (drawPane.erasedSplineList.size() != 0) {
            		drawPane.splineList.add(drawPane.erasedSplineList.get(drawPane.erasedSplineList.size()-1));
            		drawPane.erasedSplineList.remove(drawPane.erasedSplineList.size()-1);
                	drawPane.repaint();
            	}
            	System.out.println("splineList:" + drawPane.splineList.size());
            	System.out.println("erasedSplineList:" + drawPane.erasedSplineList.size());
            	
            }
        });
		
		erase_all.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (drawPane.splineList != null) drawPane.splineList.clear();
            	if (drawPane.currentSpline != null) drawPane.currentSpline.clear();
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
		
		widthInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String inputWidth = null;
				while (true) {
					inputWidth = JOptionPane.showInputDialog(null, "펜의 굵기를 입력하세요.", "catchmind", JOptionPane.QUESTION_MESSAGE);
					
					if (inputWidth == null) {
						return;
					}
					
					try {
						if (Integer.parseInt(inputWidth) > 0) {
							break;
						}
					} catch (NumberFormatException err) {
						
					}
				}
				EditorEx.this.penWidth = Integer.parseInt(inputWidth);
			}
		});
        
        
        
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());

        drawPane = new DrawingPanel();
        drawPane.setBackground(Color.WHITE);
        c.add(drawPane, BorderLayout.CENTER);
        
               
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("hello.java");
        model.addElement("world.java");
        
		JList<String> list = new JList<>(model);
		
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setPreferredSize(new Dimension(150, 100));
		
		c.add(listScrollPane, BorderLayout.EAST);

		pack(); 
		setSize(1024, 712);
        setVisible(true);
    }

    
    //개중요
    class DrawingPanel extends JPanel{
    	
    	private Vector<Vector<MyPoint>> splineList;
    	private Vector<Vector<MyPoint>> erasedSplineList;
    	private Vector<MyPoint> currentSpline = null;

    	MyPoint start = null; 
    	MyPoint end = null; 
    	
    	DrawingPanel(){
    		splineList = new Vector<Vector<MyPoint>>();
    		erasedSplineList = new Vector<Vector<MyPoint>>();
    		start = new MyPoint();
    		end = new MyPoint();
    		
    		
    	   	addMouseListener(new MouseAdapter(){
        		public void mousePressed(MouseEvent e) {
        			start.x = e.getX();
        			start.y = e.getY();
				
					currentSpline = new Vector<>();
    				MyPoint p = new MyPoint(e.getX(), e.getY());
    				currentSpline.add(p);
    				splineList.add(currentSpline);
    				erasedSplineList.clear(); //새 Spline 작성 시 Redo 초기화(중첩 방지)
        	   		p.setColor(EditorEx.this.currentColor);
        	   		p.setWidth(EditorEx.this.penWidth);
        		}
        		
    	   	});	
    	   	
    	   	addMouseMotionListener(new MouseMotionAdapter() {
    	   		public void mouseDragged(MouseEvent e) {
    				MyPoint p = new MyPoint(e.getX(), e.getY());
        	   		p.setColor(EditorEx.this.currentColor);
        	   		p.setWidth(EditorEx.this.penWidth);
    				currentSpline.add(p);
    				repaint();
    	   		}
    	   	});
    	}
    	
    	public void paintComponent(Graphics g) {
    		super.paintComponent(g);
			
			for(Vector<MyPoint> spline : splineList) {
				Graphics2D g2 = (Graphics2D) g;
				
				for (int i = 1; i < spline.size(); i++) {
					MyPoint temp1 = spline.get(i-1);
					MyPoint temp2 = spline.get(i);
					g.setColor(temp1.pointColor);
		    		g2.setStroke(new BasicStroke(temp1.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g.drawLine(temp1.x, temp1.y, temp2.x, temp2.y);
				}
			}

			
    	}

    }
    
    public static void main(String[] args) {
        new EditorEx();
    }
}