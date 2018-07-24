using Uno;
using Uno.Collections;
using Fuse;

namespace Fuse.Bluetooth
{

	
	public class MessageEventArgs : EventArgs 
	{

	    private String message;

        public MessageEventArgs(String message)
        {
            this.message = message;
        }

        // This is a straightforward implementation for 
        // declaring a public field
        public String Message
        {
            get
            {
                return message;
            }
        }
    }


	
}
