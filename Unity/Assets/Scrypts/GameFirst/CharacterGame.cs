using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

namespace Assets.Scrypts
{
    public class CharacterGame : MonoBehaviour
    {
        public static List<Chest> listChests;
        public static List<EnemyBot> listEnemy;
        public static GameObject objCharacter, objChest, objEnemy;
        private float x;
        private float z;
        public static bool roundFirst, roundSecond = false;
        public static bool isSpawn = false;
        public static bool isTimer = false;
        private GameServer gameServer;
        public Text timer;
        private int time;


        void Start()
        {
            objCharacter = GameObject.FindGameObjectWithTag("Player");
            objChest = Resources.Load<GameObject>("Chest") as GameObject;
            objEnemy = Resources.Load<GameObject>("Enemy") as GameObject;
            listChests = new List<Chest>();
            listEnemy = new List<EnemyBot>();
            gameServer = new GameServer();
            gameServer.StartClient("93.100.216.84", 3001);
            gameServer.ConnectToServer();
            gameServer.StartMain();
        }

        void Update()
        {
            if (isSpawn)
            {
                SetChest();
                SetEnemy();
                Account.character.health = 100;
                isSpawn = false;
            }

            if (roundFirst)
            {
                if (!isTimer)
                {
                    isTimer = true;
                    StartCoroutine(Timer());
                }

                x = Input.GetAxisRaw("Horizontal");
                z = Input.GetAxisRaw("Vertical");
                objCharacter.transform.position += new Vector3(x, 0, z);
                if (Input.GetKey(KeyCode.W))
                    gameServer.SendStep("W");
                if (Input.GetKey(KeyCode.S))
                    gameServer.SendStep("S");
                if (Input.GetKey(KeyCode.A))
                    gameServer.SendStep("A");
                if (Input.GetKey(KeyCode.D))
                    gameServer.SendStep("D");

                if (Input.GetKeyDown(KeyCode.E))
                {
                    gameServer.SendStep("DOWN_E");
                }

                if (Input.GetKeyUp(KeyCode.E))
                {
                    gameServer.SendStep("UP_E");
                }
            }
            else if (roundSecond)
            {
                SceneManager.LoadScene("SecondGame");
            }
        }

        IEnumerator Timer()
        {
            time = 60;

            while (time-- > 0)
            {
                timer.text = time.ToString();
                yield return new WaitForSeconds(1);
            }
            roundFirst = false;
        }

        public static void SetChest()
        {
            foreach (Chest chest in listChests)
            {
                var o = Instantiate(objChest, new Vector3(chest.x, 0, chest.z), Quaternion.identity);
                o.transform.localScale = new Vector3(3f, 3f, 3f);
            }
        }

        public void SetEnemy()
        {
            foreach (EnemyBot enemy in listEnemy)
            {
                var o = Instantiate(objEnemy, new Vector3(enemy.x, 10, enemy.z), Quaternion.identity);
                o.transform.localScale = new Vector3(0.1f, 0.1f, 0.1f);
            }
        }

        public bool CheckChest(float x, float z)
        {
            foreach (Chest elem in listChests)
            {
                if (elem.x >= x)
                {
                    if (elem.x - x <= 30)
                    {
                        if (elem.z >= z)
                        {
                            if (elem.z - z <= 30) return false;
                        }
                        else
                        {
                            if (z - elem.z <= 30) return false;
                        }
                    }
                }
                else
                {
                    if (x - elem.x <= 30)
                    {
                        if (elem.z >= z)
                        {
                            if (elem.z - z <= 30) return false;
                        }
                        else
                        {
                            if (z - elem.z <= 30) return false;
                        }
                    }
                }
            }
            return true;
        }

        public static void WaitTheGame()
        {
            Debug.Log("Loading game, please wait");
        }

        public static Chest SearchChest(float x, float z)
        {
            foreach (Chest chest in listChests)
            {
                if (chest.x == x && chest.z == z)
                {
                    return chest;
                }
            }
            return null;
        }

        public static EnemyBot SearchEnemy(float x, float z)
        {
            foreach (EnemyBot chest in listEnemy)
            {
                if (chest.x == x && chest.z == z)
                {
                    return chest;
                }
            }
            return null;
        }
    }
}