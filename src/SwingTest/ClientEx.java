package SwingTest;
import javax.swing.*;
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
import SwingTest.EditorEx.DrawingPanel;
import SwingTest.EditorEx.ServerThread;

import java.awt.Color;

public class ClientEx extends JFrame {

	private Color currentColor = Color.BLACK; 
	private DrawingPanel drawPane = null;
	private int penWidth = 3;
    private String answer = "";
//    private String nickname = "";
    private BufferedReader in = null;
	private BufferedWriter out = null;
	private Socket socket = null;
	private DefaultListModel<String> model = new DefaultListModel<>();
	private JScrollPane listScrollPane;
	private JTextArea consolePane;
	
    public ClientEx() {
    	
    	setTitle("Client");
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
            	currentColor = JColorChooser.showDialog(ClientEx.this, "Select Color", Color.BLACK);

            }
        });
		
		widthInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String inputWidth = null;
				while (true) {
					inputWidth = JOptionPane.showInputDialog(null, "펜의 굵기를 입력하세요.", "Server", JOptionPane.QUESTION_MESSAGE);
					
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
				ClientEx.this.penWidth = Integer.parseInt(inputWidth);
			}
		});
		
		
        
        //layout
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());

        drawPane = new DrawingPanel();
        
        //설레는 댓글창
        this.consolePane = new JTextArea();
        this.consolePane.setLineWrap(true);        // 자동 줄바꿈
        this.consolePane.setWrapStyleWord(true);
        JScrollPane consoleScrollPane = new JScrollPane(this.consolePane);
        consoleScrollPane.setPreferredSize(new Dimension(200, 100));
        consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        c.add(consoleScrollPane, BorderLayout.SOUTH); 
        
        model = new DefaultListModel<>();
        
		JList<String> list = new JList<>(model);
		list.setEnabled(false);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setFixedCellWidth(150);  
		model.addElement("***************************************");
		model.addElement("Sys: 정답을 입력해주세요.");
		model.addElement("***************************************");
		
		this.listScrollPane = new JScrollPane(list);
		this.listScrollPane.setPreferredSize(new Dimension(200, 100));
        this.listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.add(this.listScrollPane, BorderLayout.EAST);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(listScrollPane, BorderLayout.CENTER);
		rightPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        drawPane.setBackground(Color.WHITE);
		c.add(rightPanel, BorderLayout.EAST);
        c.add(drawPane, BorderLayout.CENTER);
        
        
        //텍스트박스 내 댓글을 ListPane에 올리기
//        InputMap im = consolePane.getInputMap(JComponent.WHEN_FOCUSED);
//        ActionMap am = consolePane.getActionMap();
//
//        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
//
//        am.put("enterPressed", new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//                String text = consolePane.getText();
//                
//                try {
//                    // 1. GUI에 메시지 등록 (Enter 키를 누르면 바로 등록되어야 함)
//                	model.addElement(nickname + ": " + text);
//            		consolePane.setText(""); // 입력창 초기화
//                    
//                    if (out != null) {
//                    	out.write(text + "\n"); // 서버가 readLine() 하도록 \n 추가
//                    	out.flush();
//                    } else {
//                        model.addElement("[오류: 서버에 연결되지 않음]");
//                    }
//                    
//                    // 3. 스크롤 조정 (안정화)
//                    SwingUtilities.invokeLater(() -> {
//                        JScrollBar bar = listScrollPane.getVerticalScrollBar();
//                        bar.setValue(bar.getMaximum());
//                    });
//	                    
//                } catch (IOException ele) {
//            		System.out.println("메시지 전송 오류: " + ele.getMessage());
//            		model.addElement("[네트워크 전송 오류]");
//                }
//            }
//        });
//        
		pack(); 
		setSize(1024, 712);
        setVisible(true);
        

        new ClientThread().start();
        
//        while(true) {
//        	nickname = JOptionPane.showInputDialog(null, "게임에 사용할 닉네임을 입력하세요.", nickname, JOptionPane.QUESTION_MESSAGE);
//        	if (!nickname.equals("")) {
//        		break;
//        	}
//        }
////        
//        //server
//        
//		try {
//			socket = new Socket("localhost", 9999); // 클라이언트 소켓 생성. 서버에 연결
//			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//			model.addElement("Admin: User와 연결되었습니다.");
//				
//		} catch (IOException e) {
//			System.out.println(e.getMessage());
//		}
    }

    class ClientThread extends Thread {
    	 @Override
         public void run() {
             try {
                 socket = new Socket("localhost", 9999);
                 SwingUtilities.invokeLater(() -> model.addElement("Sys: Server와 연결되었습니다."));
                 
                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 
                 InputMap im = consolePane.getInputMap(JComponent.WHEN_FOCUSED);
                 ActionMap am = consolePane.getActionMap();
                 
//                 new AnswerReceiveThread().start();
   
                 im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
   
                 am.put("enterPressed", new AbstractAction() {
                   @Override
                   public void actionPerformed(ActionEvent e) {
       
                       String text = consolePane.getText();
                       
                       try {
                           // 1. GUI에 메시지 등록 (Enter 키를 누르면 바로 등록되어야 함)
                    	   model.addElement("Client: " + text);
                    	   consolePane.setText(""); // 입력창 초기화
                           
                           if (out != null) {
                        	   out.write(text + "\n"); // 서버가 readLine() 하도록 \n 추가
                        	   out.flush();
                           } else {
                               model.addElement("[오류: 서버에 연결되지 않음]");
                           }
                           
                           // 3. 스크롤 조정 (안정화)
                           SwingUtilities.invokeLater(() -> {
                               JScrollBar bar = listScrollPane.getVerticalScrollBar();
                               bar.setValue(bar.getMaximum());
                           });
       	                    
                       } catch (IOException ele) {
                    	   System.out.println("메시지 전송 오류: " + ele.getMessage());
                    	   model.addElement("[네트워크 전송 오류]");
                       }
                   }
               });
             } catch (IOException e) {
                 SwingUtilities.invokeLater(() -> model.addElement("Sys: 서버/연결 오류 발생 - " + e.getMessage()));
             }
         }
    }
    
//    class AnswerReceiveThread extends Thread {
//    	private BufferedReader answerIn;
//    	public void run() {
//    		try {
//    			String answer;
//    	    	while(true) {
//    	    		answer = answerIn.readLine();
//    	            System.out.println(answer);
//    	    	}
//    		} catch (IOException e) {
//	            System.out.println(e.getMessage());
//                
//            }
//    	}
//    }
    
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
        	   		p.setColor(ClientEx.this.currentColor);
        	   		p.setWidth(ClientEx.this.penWidth);
    				currentSpline.add(p);
    				splineList.add(currentSpline);
    				repaint();
        		}
        		
    	   	});	
    	   	
    	   	addMouseMotionListener(new MouseMotionAdapter() {
    	   		public void mouseDragged(MouseEvent e) {
    				MyPoint p = new MyPoint(e.getX(), e.getY());
        	   		p.setColor(ClientEx.this.currentColor);
        	   		p.setWidth(ClientEx.this.penWidth);
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
        new ClientEx();
    }
}