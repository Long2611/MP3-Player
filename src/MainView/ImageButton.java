package MainView;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageButton{
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Image Button Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);

            JButton imageButton = createImageButton("path/to/your/image.png");
            frame.add(imageButton, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }

    private static JButton createImageButton(String imagePath) {
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));

            // Resize the image to fit the button
            int width = 50; // Set your desired width
            int height = 50; // Set your desired height
            Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);

            ImageIcon icon = new ImageIcon(resizedImg);
            JButton button = new JButton(icon);
            
            // Set the preferred size for the button
            button.setPreferredSize(new Dimension(width, height));

            return button;
        } catch (IOException e) {
            e.printStackTrace();
            return new JButton("Error loading image");
        }
    }
}
