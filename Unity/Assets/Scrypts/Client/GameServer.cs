using System.Globalization;

namespace Assets.Scrypts
{
    class GameServer : Client, SendMessage
    {
        bool loagingGame = false;

        public GameServer() { }

        public void ConnectToServer()
        {
            string data = Account.character.mail + "#" + Account.character.password;
            connection.Send(new Message(MessageType.AUTHORIZATION, data));

            while (true)
            {
                Message message = connection.Receive();
                if (message == null)
                {
                    ServerClose();
                    return;
                }
                if (message.type == MessageType.AUTHORIZATION)
                {
                    NotifyConnectionStatusChanged(true);
                    ConsoleHelper.WriteMessage("Account accept.");
                    break;
                }
                online = true;
            }
        }

        public override void ClientMainLoop()
        {
            while (true)
            {
                Message message = connection.Receive();

                if (message != null)
                {
                    if (message.type == MessageType.LOADING_GAME && !loagingGame)
                    {
                        CharacterGame.WaitTheGame();
                        message = connection.Receive();
                        loagingGame = true;
                    }
                    if (message.type == MessageType.SET_INFO)
                    {
                        while (true)
                        {
                            message = connection.Receive();
                            if (message != null)
                            {
                                if (message.type == MessageType.SET_CHEST)
                                {
                                    Chest chest = Converter.XmlToChest(message.data);
                                    CharacterGame.listChests.Add(chest);
                                }
                                if (message.type == MessageType.SET_ENEMY)
                                {
                                    EnemyBot enemy = Converter.XmlToEnemy(message.data);
                                    CharacterGame.listEnemy.Add(enemy);
                                }
                                if (message.type == MessageType.SET_END)
                                {
                                    CharacterGame.isSpawn = true;
                                    connection.Send(new Message(MessageType.READY));
                                    break;
                                }
                            }
                        }
                    }

                    if (message.type == MessageType.SET_CARD_START)
                    {
                        ConsoleHelper.WriteMessage("Set card!");
                        while (true)
                        {
                            message = connection.Receive();
                            if (message != null)
                            {
                                if (message.type == MessageType.SET_CARD_HAND)
                                {
                                    Item item = Converter.XmlToCard(message.data);
                                    GameManagerScr.playerHand.Add(new Card(item.name, item.damage, item.health));
                                }
                                if (message.type == MessageType.SET_CARD_DECK)
                                {
                                    GameManagerScr.countDeckPlayer = int.Parse(message.data);
                                }
                                if (message.type == MessageType.SET_CARD_TABLE)
                                {
                                    Item item = Converter.XmlToCard(message.data);
                                    GameManagerScr.playerTable.Add(new Card(item.name, item.damage, item.health));
                                }
                                if (message.type == MessageType.SET_END)
                                {
                                    //CharacterGame.isSpawn = true;
                                    connection.Send(new Message(MessageType.READY));
                                    break;
                                }
                            }
                        }
                    }

                    if (message.type == MessageType.GOT_CHEST)
                    {
                        string[] data = message.data.Split('#');
                        Chest chest = CharacterGame.SearchChest(float.Parse(data[0], CultureInfo.InvariantCulture.NumberFormat), float.Parse(data[1], CultureInfo.InvariantCulture.NumberFormat));
                        if (chest != null)
                        {
                            foreach (Item item in chest.listItem)
                            {
                                //ConsoleHelper.WriteMessage(item.ToString());
                            }
                        }
                    }
                    if (message.type == MessageType.GOT_ENEMY)
                    {
                        string[] data = message.data.Split('#');
                        EnemyBot enemy = CharacterGame.SearchEnemy(float.Parse(data[0], CultureInfo.InvariantCulture.NumberFormat), float.Parse(data[1], CultureInfo.InvariantCulture.NumberFormat));
                        if (enemy != null)
                        {
                            enemy.attack(Account.character);
                            //ConsoleHelper.WriteMessage(Account.character.health + " HP");
                        }
                    }
                    if (message.type == MessageType.START)
                    {
                        CharacterGame.roundFirst = true;
                        loagingGame = false;
                    }
                    if (message.type == MessageType.ROUND_END)
                    {
                        CharacterGame.roundFirst = false;
                        CharacterGame.roundSecond = true;
                    }
                    online = true;
                }
                else
                {
                    ServerClose();
                    return;
                }
            }
        }

        public void SendStep(string step)
        {
            switch (step)
            {
                case "W":
                    SendTextMessage(new Message(MessageType.W));
                    break;
                case "A":
                    connection.Send(new Message(MessageType.A));
                    break;
                case "S":
                    connection.Send(new Message(MessageType.S));
                    break;
                case "D":
                    connection.Send(new Message(MessageType.D));
                    break;
                case "DOWN_E":
                    connection.Send(new Message(MessageType.DOWN_E));
                    break;
                case "UP_E":
                    connection.Send(new Message(MessageType.UP_E));
                    break;
            }
        }
    }
}
