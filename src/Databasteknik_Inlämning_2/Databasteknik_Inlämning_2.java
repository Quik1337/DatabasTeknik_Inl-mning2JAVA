package Databasteknik_Inlämning_2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

public class Databasteknik_Inlämning_2
{
    public static void main(String[] args) throws SQLException, ClassNotFoundException, FileNotFoundException, IOException
    {
        Class.forName("com.mysql.jdbc.Driver");
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement prepstmt = null;
        
        String kundId = "";
        String skorId = "";
        String beställningId = "";
        
        int i = 0;
        
        Scanner scanner = new Scanner(System.in, "ISO-8859-1");
        
        try
        {
            //Sätter upp connection
            
            Properties p = new Properties();
            
            p.load(new FileInputStream("src/Databasteknik_Inlämning_2/Settings.properties"));
            
            con = DriverManager.getConnection(
                p.getProperty("connectionString"),
                p.getProperty("name"),
                p.getProperty("password"));

            stmt = con.createStatement();
            
            //--------------------------------------------------------------------
            
            // Skriv ut alla kunder och ta in ett användar-input (förnamn)
            
            rs = stmt.executeQuery(
                "SELECT förnamn, efternamn "
              + "FROM kund "
              + "ORDER BY förnamn");

            i = 1;
            
            while (rs.next())
            {
                String kundFörnamn = rs.getString("förnamn");
                String kundEfternamn = rs.getString("efternamn");
                
                System.out.println("[" + i + "] " + kundFörnamn + " " + kundEfternamn);
                
                i++;
            }
            
            System.out.print("\nVälj en kund genom att skriva in ett förnamn: ");
            
            String kundNamn = scanner.nextLine();
            
            //----------------------------------------------
            
            // Tar ut kund.id genom användar-input (förnamn)
            
            prepstmt = con.prepareStatement(
                "SELECT id "
              + "FROM kund "
              + "WHERE förnamn = ?");
            
            prepstmt.setString(1, kundNamn);
            rs = prepstmt.executeQuery();
            
            while (rs.next())
            {
                kundId = rs.getString("id");
            }
                        
            System.out.println();
            
            //------------------------------------------------
            
            // Skriv ut alla skor och ta in användar-input för ett skonamn
            
            rs = stmt.executeQuery(
                "SELECT skor.namn, märke.namn, färg.namn, skor.storlek "
              + "FROM skor "
              + "INNER JOIN märke ON skor.märkeId = märke.id "
              + "INNER JOIN färg ON skor.färgId = färg.id "
              + "ORDER BY skor.namn");

            i = 1;
            
            while (rs.next())
            {
                String skorNamn = rs.getString("skor.namn");
                String märkeNamn = rs.getString("märke.namn");
                String färgNamn = rs.getString("färg.namn");
                String skorStorlek = rs.getString("skor.storlek");
                
                System.out.println("[" + i + "] " +
                                   "Namn: " + skorNamn + " | " +
                                   "Märke: " + märkeNamn + " | " +
                                   "Färg: " + färgNamn + " | " +
                                   "Storlek: " + skorStorlek);
                
                i++;
            }
            
            System.out.print("\nVälj ett par skor genom att skriva in ett namn: ");
            
            String skorNamn = scanner.nextLine();
            
            //--------------------------------------------------------------------------
            //Tar fram skor.id från användar-input(skorNamn)
            
            prepstmt = con.prepareStatement(
                "SELECT id "
              + "FROM skor "
              + "WHERE namn = ?");
            
            prepstmt.setString(1, skorNamn);
            rs = prepstmt.executeQuery();
            
            while (rs.next())
            {
                skorId = rs.getString("id");
                
                //System.out.println(skorId);
            }
                        
            System.out.println();
            
            //------------------------------------------------------------------
            // Hämtar en kunds beställnigar (genom id från det förnamnet användaren skrev in)
            // där expedierad är false. Skriver sedan ut dem. 
            
            prepstmt = con.prepareStatement(
                "SELECT datum "
              + "FROM beställning "
              + "WHERE kundId = ? AND expedierad = ? "
              + "GROUP BY datum");
            
            prepstmt.setString(1, kundId);
            prepstmt.setBoolean(2, false);
            rs = prepstmt.executeQuery();
            
            System.out.println("*Beställningar*");
            
            i = 1;
            
            while (rs.next())
            {
                String datum = rs.getString("datum");
                
                System.out.println("[" + i + "] " + datum);
                
                i++;
            }
            
            System.out.println("[*] Ny beställning");
                        
            System.out.println();
            
            //------------------------------------------------------------------
            
            // Om användaren skriver in "Ny beställning" skickas null in som
            // beställning.id i Stored Proceduren.
            // Om användaren skriver in något av datumen hämtas idt för den beställningen.
            
            System.out.print("Vilken beställning vill du lägga dina skor i?: ");
            
            String beställning = scanner.nextLine();
            
            if (beställning.equals("Ny beställning"))
            {
                beställningId = null;
            }
            
            else
            {
                prepstmt = con.prepareStatement(
                    "SELECT id "
                  + "FROM beställning "
                  + "WHERE datum = ? AND expedierad = ? AND kundId = ?");
                
                prepstmt.setString(1, beställning);
                prepstmt.setBoolean(2, false);
                prepstmt.setString(3, kundId);

                rs = prepstmt.executeQuery();

                while (rs.next())
                {
                    beställningId = rs.getString("id");
                }
            }
            
            //System.out.println(beställningId + "\n");
            
            //------------------------------------------------------------------
            
            // Skickar in de värden som hämtats in i Stored Proceduren.
            
            CallableStatement stm = con.prepareCall("CALL addToCart(?, ?, ?)");
            stm.setString(1, kundId);
            stm.setString(2, skorId);
            stm.setString(3, beställningId);
            stm.execute();
            
            ResultSet rs1 = stm.getResultSet();
            
            String s = "";

            while(rs1.next())
            {
                s = rs1.getString("feedback");
            }       

            System.out.println("\n" + s);
            
            //---------------------------------------------------------
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally 
        {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if (con != null)
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }
}
 