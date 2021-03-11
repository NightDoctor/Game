using System;
using System.IO;
using System.Net.Sockets;

namespace Assets.Scrypts
{
    public class Connection
    {
        StreamWriter writer;
        StreamReader reader;
        NetworkStream stream;

        public Connection(TcpClient tcpClient)
        {
            stream = tcpClient.GetStream();
            writer = new StreamWriter(stream);
            reader = new StreamReader(stream);
        }

        public void Send(Message message)
        {
            writer.WriteLine(message.xml);
            try
            {
                writer.Flush();
            }
            catch (Exception)
            {
                writer.Flush();
            }
        }

        public Message Receive()
        {
            Message message;

            try
            {
                string stringLine = reader.ReadLine();
                if (stringLine == null)
                {
                    return null;
                }
                message = Converter.XmlToObject(stringLine);
            }
            catch (Exception)
            {
                return null;
            }
            return message;
        }

        public void Close()
        {
            stream.Close();
        }
    }
}