using UnityEngine;

public class Card//maincamera success
{
    public string Name;
    public Sprite Logo { get; set; }
    public int Attack, Defense;
    public bool CanAttack;
    public bool IsPlaced;

    public bool IsAlive
    {
        get
        {
            return Defense > 0;
        }
    }

    public Card(string name, int attack, int defense)
    {
        Name = name;
        Attack = attack;
        Defense = defense;
        CanAttack = false;
        IsPlaced = false;
    }

    public void ChangeAttackState(bool can)
    {
        CanAttack = can;
    }

    public void GetDamage(int damage)
    {
        Defense -= damage;
    }
}