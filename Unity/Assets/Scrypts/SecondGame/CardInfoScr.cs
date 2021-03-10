using UnityEngine;
using UnityEngine.UI;

public class CardInfoScr : MonoBehaviour// card
{
    public Card SelfCard;
    public Image Logo;
    public Text Name, Attack, Defense;
    public GameObject HideObj, HighLightObj;
    public bool isPlayer;

    public void HideCardInfo(Card card)
    {
        SelfCard = card;
        HideObj.SetActive(true);
        isPlayer = false;
    }

    public void ShowCardInfo(Card card, bool isPlayer)
    {
        this.isPlayer = isPlayer;
        HideObj.SetActive(false);
        SelfCard = card;

        Logo.sprite = card.Logo;
        Logo.preserveAspect = true;
        Name.text = card.Name;

        RefreshData();
    }

    public void RefreshData()
    {
        Attack.text = SelfCard.Attack.ToString();
        Defense.text = SelfCard.Defense.ToString();
    }

    public void HighLightCard()
    {
        HighLightObj.SetActive(true);
    }

    public void DeHighLightCard()
    {
        HighLightObj.SetActive(false);
    }
}
