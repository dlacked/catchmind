package SwingTest;
import javax.swing.*;
//import com.google.gson.Gson;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import SwingTest.MyPoint;

import java.awt.Color;

public class EditorEx extends JFrame {
	
	private Color currentColor = Color.BLACK; 
	private DrawingPanel drawPane = null;
	private int penWidth = 3;
    private String answer = "";
//    private String nickname = "";
    private BufferedReader in = null;
	private BufferedWriter out = null;
	private ServerSocket listener = null;
	private Socket socket = null;
	private String clientMessage = null;
	private DefaultListModel<String> model = new DefaultListModel<>();
	private JScrollPane listScrollPane;
	
    public EditorEx() {
    	
    	setTitle("Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
		JMenuBar menubar = new JMenuBar();
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
    				
    				if (out != null) {
    					try {
    						out.write("UNDO" + "\n");
    						out.flush();
    					} catch (IOException ele) {
    						
    					}
    				}
            		
                	drawPane.repaint();
            	}
            	
            }
        });
		
		redo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (drawPane.erasedSplineList.size() != 0) {
            		drawPane.splineList.add(drawPane.erasedSplineList.get(drawPane.erasedSplineList.size()-1));
            		drawPane.erasedSplineList.remove(drawPane.erasedSplineList.size()-1);
                	drawPane.repaint();

    				if (out != null) {
    					try {
    						out.write("REDO" + "\n");
    						out.flush();
    					} catch (IOException ele) {
    						
    					}
    				}
            	}
            	
            }
        });
		
		erase_all.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (drawPane.splineList != null) drawPane.splineList.clear();
            	if (drawPane.currentSpline != null) drawPane.currentSpline.clear();
            	drawPane.repaint();
            	

				if (out != null) {
					try {
						out.write("ERASEALL" + "\n");
						out.flush();
					} catch (IOException ele) {
						
					}
				}
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

        
		JList<String> list = new JList<>(model);
		list.setEnabled(false);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setFixedCellWidth(150);  
		model.addElement("***************************************");
		model.addElement("그림을 그려주세요.");
		model.addElement("***************************************");
		
		listScrollPane = new JScrollPane(list);
		
		listScrollPane.setPreferredSize(new Dimension(200, 100));
        listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.add(listScrollPane, BorderLayout.EAST);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(listScrollPane, BorderLayout.CENTER);
//		rightPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        drawPane.setBackground(Color.WHITE);
		c.add(rightPanel, BorderLayout.EAST);
        c.add(drawPane, BorderLayout.CENTER);
        
		pack(); 
		setSize(1024, 712);
        setVisible(true);
        
        new ServerThread().start();
        
    }

    class ServerThread extends Thread {
        @Override
        public void run() {
            try {
                listener = new ServerSocket(9999);
                SwingUtilities.invokeLater(() -> model.addElement("사용자를 기다리는 중...")); //GUI 컴포넌트 업데이트를 위함
                
                socket = listener.accept();
                SwingUtilities.invokeLater(() -> model.addElement("Client와 연결되었습니다."));
                
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String answer = "";
                while(true) {
                	while(true) {

                		answer = JOptionPane.showInputDialog(null, "정답을 입력하세요.", "Server", JOptionPane.QUESTION_MESSAGE);
                        if (answer != null && !answer.trim().isEmpty()) {
//                        	//
                            out.write(answer + "\n");
                            out.flush();
//                            //
                            break;
                        }
                		
                	}
                    
                    EditorEx.this.answer = answer;
                    
                    String clientMessage;
                    while (true) {
                        clientMessage = in.readLine();
                        if (clientMessage == null) break; 
                        // GUI 업데이트는 반드시 EDT에서
                        final String msg = clientMessage;
                        SwingUtilities.invokeLater(() -> {
                            model.addElement("Client: " + msg);
                            JScrollBar bar = listScrollPane.getVerticalScrollBar();
                            bar.setValue(bar.getMaximum());
                        });
                        
                        if (clientMessage.equals(answer)) {
                    		model.addElement("***************************************");
                    		model.addElement("Client가 정답을 맞췄습니다!");
                    		model.addElement("***************************************");
                        	if (drawPane.splineList != null) drawPane.splineList.clear();
                        	if (drawPane.currentSpline != null) drawPane.currentSpline.clear();
                        	drawPane.repaint();
                        	break;
                        }
                        
                    }
                }
                
                
                
            } catch (IOException e) {
                
            } finally {
                try {
                    if (socket != null) socket.close();
                    if (listener != null) listener.close();
                } 
                catch (IOException e) { 
                }
            }
        }
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
				
					currentSpline = new Vector<>();
    				MyPoint p = new MyPoint(e.getX(), e.getY());
    				erasedSplineList.clear(); //새 Spline 작성 시 Redo 초기화
        	   		p.setColor(EditorEx.this.currentColor);
        	   		p.setWidth(EditorEx.this.penWidth);
    				currentSpline.add(p);
    				splineList.add(currentSpline);
    				repaint();
    				
    				if (out != null) {
    					try {
    						out.write("PRESSED " + p.x + " " + p.y + " " + p.pointColor.getRGB() + " " + p.width + "\n");
    						
    						out.flush();
    					} catch (IOException ele) {
    						
    					}
    				}
        		}
        		
    	   	});	
    	   	
    	   	addMouseMotionListener(new MouseMotionAdapter() {
    	   		public void mouseDragged(MouseEvent e) {
    				MyPoint p = new MyPoint(e.getX(), e.getY());
        	   		p.setColor(EditorEx.this.currentColor);
        	   		p.setWidth(EditorEx.this.penWidth);
    				currentSpline.add(p);
    				repaint();
    				if (out != null) {
    					try {
    						out.write("DRAGGING " + p.x + " " + p.y + " " + p.pointColor.getRGB() + " " + p.width + "\n");
    						out.flush();
    					} catch (IOException ele) {
    						
    					}
    				}
    	   		}
    	   	});
    	   	
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