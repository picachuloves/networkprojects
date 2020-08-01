public class Main {
    public static void main(String[] args) {
        try
        {
            CopiesFinder finder = new CopiesFinder(args[0], 5555);
            finder.findCopies();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}

