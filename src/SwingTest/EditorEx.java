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

		penOption.add(widthInput);
		penOption.add(colorSelect);
		
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
//            	System.out.println("splineList:" + drawPane.splineList.size());
//            	System.out.println("erasedSplineList:" + drawPane.erasedSplineList.size());
            	
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
		
		
        
        //layout
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());

        drawPane = new DrawingPanel();
        
        //설레는 댓글창
        JTextArea  consolePane = new JTextArea();
        consolePane.setLineWrap(true);        // 자동 줄바꿈
        consolePane.setWrapStyleWord(true);
        JScrollPane consoleScrollPane = new JScrollPane(consolePane);
        consoleScrollPane.setPreferredSize(new Dimension(200, 100));
        consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        c.add(consoleScrollPane, BorderLayout.SOUTH); 
        
        DefaultListModel<String> model = new DefaultListModel<>();
        
		JList<String> list = new JList<>(model);
		list.setEnabled(false);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setFixedCellWidth(150);  
		model.addElement("***************************************");
		model.addElement("Admin: 댓글을 입력해주세요.");
		model.addElement("***************************************");
		
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setPreferredSize(new Dimension(200, 100));
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.add(listScrollPane, BorderLayout.EAST);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(listScrollPane, BorderLayout.CENTER);
		rightPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        drawPane.setBackground(Color.WHITE);
		c.add(rightPanel, BorderLayout.EAST);
        c.add(drawPane, BorderLayout.CENTER);

        
        
        
        //텍스트박스 내 댓글을 ListPane에 올리기
        InputMap im = consolePane.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = consolePane.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");

        am.put("enterPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String text = consolePane.getText();
                model.addElement("User: " + text);
                consolePane.setText("");
            }
        });
        
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
//        			start.x = e.getX();
//        			start.y = e.getY();
				
					currentSpline = new Vector<>();
    				MyPoint p = new MyPoint(e.getX(), e.getY());
    				erasedSplineList.clear(); //새 Spline 작성 시 Redo 초기화(중첩 방지)
        	   		p.setColor(EditorEx.this.currentColor);
        	   		p.setWidth(EditorEx.this.penWidth);
    				currentSpline.add(p);
    				splineList.add(currentSpline);
    				repaint();
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
    	   	
//    	   	addKeyListener(new KeyAdapter() {
//    	   		public void keyPressed(KeyEvent e) {
//    	   			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//    	   				System.out.println("엔터");
//    	   			}
//    	   		}
//    	   	});
    	}
    	
    	public void paintComponent(Graphics g) {
    		super.paintComponent(g);
			
			for(Vector<MyPoint> spline : splineList) {
				Graphics2D g2 = (Graphics2D) g;
				
				if (spline.size() == 1) {
					g.setColor(spline.get(0).pointColor);
		    		g2.setStroke(new BasicStroke(spline.get(0).width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g.drawLine(spline.get(0).x, spline.get(0).y, spline.get(0).x, spline.get(0).y);
					
					continue;
				}
				
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